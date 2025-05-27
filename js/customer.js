// =============================
// FreshPlan Customer Management
// =============================

// Initialize customer form
function initializeCustomerForm() {
    // Customer type selection
    const typeOptions = document.querySelectorAll('.type-option');
    typeOptions.forEach(option => {
        option.addEventListener('click', function() {
            typeOptions.forEach(opt => opt.classList.remove('selected'));
            this.classList.add('selected');
            
            const isChain = this.getAttribute('data-type') === 'chain';
            document.getElementById('chainQuickEntry').style.display = isChain ? 'block' : 'none';
            
            // Update branch fields when customer type changes
            updateBranchFields();
        });
    });

    // Industry selection
    document.getElementById('industry').addEventListener('change', updateBranchFields);

    // Vending interest
    document.getElementById('vendingInterest').addEventListener('change', function() {
        document.getElementById('vendingDetails').style.display = this.checked ? 'block' : 'none';
    });

    // Form submission
    document.getElementById('customerForm').addEventListener('submit', function(e) {
        e.preventDefault();
        if (validateCustomerForm()) {
            saveCustomerData();
        }
    });

    // Contract date handling
    document.getElementById('contractStart').addEventListener('change', updateContractEnd);

    // Chain locations change
    document.getElementById('totalLocations').addEventListener('change', updateChainDetails);
}

// Initialize validation
function initializeValidation() {
    // Email validation
    document.getElementById('contactEmail').addEventListener('blur', function() {
        validateEmail(this);
    });
    
    document.getElementById('salespersonEmail').addEventListener('blur', function() {
        validateEmail(this);
    });

    // Phone validation
    document.getElementById('contactPhone').addEventListener('blur', function() {
        validatePhone(this);
    });

    // ZIP code validation
    document.getElementById('zipCode').addEventListener('blur', function() {
        validateZip(this);
    });

    // Required field validation
    document.querySelectorAll('input[required], select[required]').forEach(field => {
        field.addEventListener('blur', function() {
            validateRequired(this);
        });
    });
}

// Initialize autosave
function initializeAutosave() {
    // Add autosave to all marked fields
    document.querySelectorAll('[data-autosave="true"]').forEach(field => {
        field.addEventListener('input', debounce(function() {
            autosaveData();
        }, FreshPlan.config.debounce.autosave));
    });
}

// Validate customer form
function validateCustomerForm() {
    let isValid = true;

    // Validate all required fields
    document.querySelectorAll('#customerForm input[required], #customerForm select[required]').forEach(field => {
        if (field.type === 'email') {
            if (!validateEmail(field)) isValid = false;
        } else if (field.id === 'contactPhone') {
            if (!validatePhone(field)) isValid = false;
        } else if (field.id === 'zipCode') {
            if (!validateZip(field)) isValid = false;
        } else {
            if (!validateRequired(field)) isValid = false;
        }
    });

    return isValid;
}

// Autosave data
function autosaveData() {
    const indicator = document.getElementById('autosaveIndicator');
    const indicatorText = document.getElementById('autosaveText');

    // Show saving indicator
    indicator.classList.add('saving', 'show');
    indicatorText.textContent = getTranslation('autosave.saving');

    // Collect form data
    const formData = {
        companyName: document.getElementById('companyName').value,
        legalForm: document.getElementById('legalForm').value,
        contactName: document.getElementById('contactName').value,
        contactPosition: document.getElementById('contactPosition').value,
        contactEmail: document.getElementById('contactEmail').value,
        contactPhone: document.getElementById('contactPhone').value,
        street: document.getElementById('street').value,
        zipCode: document.getElementById('zipCode').value,
        city: document.getElementById('city').value,
        industry: document.getElementById('industry').value,
        targetRevenue: document.getElementById('targetRevenue').value,
        vendingInterest: document.getElementById('vendingInterest').checked,
        timestamp: new Date().toISOString()
    };

    // Save to storage
    FreshPlan.storage.setItem(FreshPlan.config.storageKeys.autosave, JSON.stringify(formData));

    // Show saved indicator
    setTimeout(() => {
        indicator.classList.remove('saving');
        indicatorText.textContent = getTranslation('autosave.saved');
        
        // Hide after 2 seconds
        setTimeout(() => {
            indicator.classList.remove('show');
        }, 2000);
    }, 500);
}

// Load autosaved data
function loadAutosavedData() {
    const savedData = FreshPlan.storage.getItem(FreshPlan.config.storageKeys.autosave);
    if (savedData) {
        try {
            const data = JSON.parse(savedData);
            
            // Restore form fields
            Object.keys(data).forEach(key => {
                const field = document.getElementById(key);
                if (field) {
                    if (field.type === 'checkbox') {
                        field.checked = data[key];
                    } else {
                        field.value = data[key];
                    }
                }
            });

            // Show restore message
            const timestamp = new Date(data.timestamp);
            const timeAgo = Math.floor((new Date() - timestamp) / 60000);
            showMessage(`Automatisch gespeicherte Daten wiederhergestellt (vor ${timeAgo} Minuten)`, 'success');
        } catch (e) {
            console.warn('Could not restore autosaved data:', e);
        }
    }
}

// Update branch fields
function updateBranchFields() {
    const industry = document.getElementById('industry').value;
    const container = document.getElementById('branchSpecificFields');
    const chainContainer = document.getElementById('chainCategorization');
    const isChain = document.querySelector('.type-option.selected').getAttribute('data-type') === 'chain';

    // Clear existing fields
    container.innerHTML = '';
    chainContainer.innerHTML = '';

    if (!industry) return;

    // Only show single location fields if NOT a chain
    if (!isChain) {
        // Add industry-specific fields for single locations
        switch(industry) {
            case 'hotel':
                container.innerHTML = `
                    <h3>${getTranslation('customer.hotelSpecific')}</h3>
                    <div class="form-row">
                        <div class="form-group">
                            <label>${getTranslation('customer.rooms')}</label>
                            <input type="number" id="hotelRooms" min="1" data-autosave="true">
                        </div>
                        <div class="form-group">
                            <label>${getTranslation('customer.restaurantSeats')}</label>
                            <input type="number" id="restaurantSeats" min="0" data-autosave="true">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>${getTranslation('customer.occupancy')}</label>
                            <input type="number" id="occupancyRate" min="0" max="100" value="75" data-autosave="true">
                        </div>
                        <div class="form-group">
                            <label>${getTranslation('customer.breakfastGuests')}</label>
                            <input type="number" id="breakfastGuests" min="0" data-autosave="true">
                        </div>
                    </div>
                `;
                break;

            case 'altenheim':
                container.innerHTML = `
                    <h3>${getTranslation('customer.nursingSpecific')}</h3>
                    <div class="form-row">
                        <div class="form-group">
                            <label>${getTranslation('customer.residents')}</label>
                            <input type="number" id="residents" min="1" data-autosave="true">
                        </div>
                        <div class="form-group">
                            <label>${getTranslation('customer.mealsPerDay')}</label>
                            <input type="number" id="mealsPerDay" min="1" max="5" value="3" data-autosave="true">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>${getTranslation('customer.specialDiets')}</label>
                            <input type="number" id="specialDiets" min="0" max="100" value="30" data-autosave="true">
                        </div>
                        <div class="form-group">
                            <label>${getTranslation('customer.consistency')}</label>
                            <select id="consistencyLevels" data-autosave="true">
                                <option value="yes">${getTranslation('yes')}</option>
                                <option value="no">${getTranslation('no')}</option>
                            </select>
                        </div>
                    </div>
                `;
                break;

            case 'krankenhaus':
                container.innerHTML = `
                    <h3>${getTranslation('customer.hospitalSpecific')}</h3>
                    <div class="form-row">
                        <div class="form-group">
                            <label>${getTranslation('customer.beds')}</label>
                            <input type="number" id="hospitalBeds" min="1" data-autosave="true">
                        </div>
                        <div class="form-group">
                            <label>${getTranslation('customer.bedOccupancy')}</label>
                            <input type="number" id="bedOccupancy" min="0" max="100" value="85" data-autosave="true">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>${getTranslation('customer.privateRatio')}</label>
                            <input type="number" id="privatePatientsRatio" min="0" max="100" value="15" data-autosave="true">
                        </div>
                        <div class="form-group">
                            <label>${getTranslation('customer.premiumMeals')}</label>
                            <select id="premiumMeals" data-autosave="true">
                                <option value="yes">${getTranslation('yes')}</option>
                                <option value="no">${getTranslation('no')}</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>${getTranslation('customer.staffCatering')}</label>
                            <select id="staffCatering" data-autosave="true">
                                <option value="yes">${getTranslation('yes')}</option>
                                <option value="no">${getTranslation('no')}</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>${getTranslation('customer.publicCafeteria')}</label>
                            <select id="publicCafeteria" data-autosave="true">
                                <option value="yes">${getTranslation('yes')}</option>
                                <option value="no">${getTranslation('no')}</option>
                            </select>
                        </div>
                    </div>
                `;
                break;

            case 'betriebsrestaurant':
                container.innerHTML = `
                    <h3>${getTranslation('customer.canteenSpecific')}</h3>
                    <div class="form-row">
                        <div class="form-group">
                            <label>${getTranslation('customer.employees')}</label>
                            <input type="number" id="employees" min="1" data-autosave="true">
                        </div>
                        <div class="form-group">
                            <label>${getTranslation('customer.dailyDiners')}</label>
                            <input type="number" id="dailyDiners" min="1" data-autosave="true">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>${getTranslation('customer.operatingDays')}</label>
                            <input type="number" id="operatingDays" min="1" max="365" value="250" data-autosave="true">
                        </div>
                        <div class="form-group">
                            <label>${getTranslation('customer.subsidized')}</label>
                            <select id="subsidized" data-autosave="true">
                                <option value="yes">${getTranslation('yes')}</option>
                                <option value="no">${getTranslation('no')}</option>
                            </select>
                        </div>
                    </div>
                `;
                break;

            case 'restaurant':
                container.innerHTML = `
                    <h3>${getTranslation('customer.restaurantSpecific')}</h3>
                    <div class="form-row">
                        <div class="form-group">
                            <label>${getTranslation('customer.seats')}</label>
                            <input type="number" id="seats" min="1" data-autosave="true">
                        </div>
                        <div class="form-group">
                            <label>${getTranslation('customer.dailyGuests')}</label>
                            <input type="number" id="dailyGuests" min="1" data-autosave="true">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>${getTranslation('customer.eventCatering')}</label>
                            <select id="eventCatering" data-autosave="true">
                                <option value="yes">${getTranslation('yes')}</option>
                                <option value="no">${getTranslation('no')}</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>${getTranslation('customer.branches')}</label>
                            <input type="number" id="branches" min="1" value="1" data-autosave="true">
                        </div>
                    </div>
                `;
                break;
        }

        // Re-initialize autosave for new fields
        container.querySelectorAll('[data-autosave="true"]').forEach(field => {
            field.addEventListener('input', debounce(function() {
                autosaveData();
            }, FreshPlan.config.debounce.autosave));
        });
    } else {
        // Show chain categorization for chains
        updateChainCategorization(industry, chainContainer);
    }
}

// Update chain categorization
function updateChainCategorization(industry, container) {
    switch(industry) {
        case 'hotel':
            container.innerHTML = `
                <h4>${getTranslation('chain.sizeCategories')}</h4>
                <div class="size-category">
                    <label>${getTranslation('chain.smallHotel')}</label>
                    <input type="number" class="number-input" id="smallHotels" min="0" value="0">
                    <span>${getTranslation('chain.hotels')}</span>
                </div>
                <div class="size-category">
                    <label>${getTranslation('chain.mediumHotel')}</label>
                    <input type="number" class="number-input" id="mediumHotels" min="0" value="0">
                    <span>${getTranslation('chain.hotels')}</span>
                </div>
                <div class="size-category">
                    <label>${getTranslation('chain.largeHotel')}</label>
                    <input type="number" class="number-input" id="largeHotels" min="0" value="0">
                    <span>${getTranslation('chain.hotels')}</span>
                </div>
                <div class="service-matrix">
                    <h4>${getTranslation('chain.servicesEquipment')}</h4>
                    <div class="service-row">
                        <label>${getTranslation('chain.breakfastService')}</label>
                        <input type="number" id="hotelsBreakfast" min="0" class="number-input">
                        <span>${getTranslation('chain.of')} <span id="totalHotelsCount">0</span> ${getTranslation('chain.hotels')}</span>
                    </div>
                    <div class="service-row">
                        <label>${getTranslation('chain.restaurantAlaCarte')}</label>
                        <input type="number" id="hotelsRestaurant" min="0" class="number-input">
                        <span>${getTranslation('chain.of')} <span class="totalHotelsCount">0</span> ${getTranslation('chain.hotels')}</span>
                    </div>
                    <div class="service-row">
                        <label>${getTranslation('chain.roomService')}</label>
                        <input type="number" id="hotelsRoomService" min="0" class="number-input">
                        <span>${getTranslation('chain.of')} <span class="totalHotelsCount">0</span> ${getTranslation('chain.hotels')}</span>
                    </div>
                    <div class="service-row">
                        <label>${getTranslation('chain.banquetEvents')}</label>
                        <input type="number" id="hotelsBanquet" min="0" class="number-input">
                        <span>${getTranslation('chain.of')} <span class="totalHotelsCount">0</span> ${getTranslation('chain.hotels')}</span>
                    </div>
                </div>
            `;
            
            // Add event listeners for chain calculations
            setTimeout(() => {
                ['smallHotels', 'mediumHotels', 'largeHotels'].forEach(id => {
                    document.getElementById(id).addEventListener('input', updateHotelCount);
                });
            }, 100);
            break;

        case 'altenheim':
            container.innerHTML = `
                <h4>${getTranslation('chain.sizeCategories')}</h4>
                <div class="size-category">
                    <label>${getTranslation('chain.smallNursing')}</label>
                    <input type="number" class="number-input" id="smallNursing" min="0" value="0">
                    <span>${getTranslation('chain.homes')}</span>
                </div>
                <div class="size-category">
                    <label>${getTranslation('chain.mediumNursing')}</label>
                    <input type="number" class="number-input" id="mediumNursing" min="0" value="0">
                    <span>${getTranslation('chain.homes')}</span>
                </div>
                <div class="size-category">
                    <label>${getTranslation('chain.largeNursing')}</label>
                    <input type="number" class="number-input" id="largeNursing" min="0" value="0">
                    <span>${getTranslation('chain.homes')}</span>
                </div>
                <div class="service-matrix">
                    <h4>${getTranslation('chain.servicesEquipment')}</h4>
                    <div class="service-row">
                        <label>${getTranslation('chain.fullMeals')}</label>
                        <input type="number" id="nursingFullService" min="0" class="number-input">
                        <span>${getTranslation('chain.of')} <span id="totalNursingCount">0</span> ${getTranslation('chain.homes')}</span>
                    </div>
                    <div class="service-row">
                        <label>${getTranslation('chain.snacks')}</label>
                        <input type="number" id="nursingSnacks" min="0" class="number-input">
                        <span>${getTranslation('chain.of')} <span class="totalNursingCount">0</span> ${getTranslation('chain.homes')}</span>
                    </div>
                </div>
            `;
            
            setTimeout(() => {
                ['smallNursing', 'mediumNursing', 'largeNursing'].forEach(id => {
                    document.getElementById(id).addEventListener('input', updateNursingCount);
                });
            }, 100);
            break;

        case 'krankenhaus':
            container.innerHTML = `
                <h4>${getTranslation('chain.sizeCategories')}</h4>
                <div class="size-category">
                    <label>${getTranslation('chain.smallHospital')}</label>
                    <input type="number" class="number-input" id="smallHospitals" min="0" value="0">
                    <span>${getTranslation('chain.clinics')}</span>
                </div>
                <div class="size-category">
                    <label>${getTranslation('chain.mediumHospital')}</label>
                    <input type="number" class="number-input" id="mediumHospitals" min="0" value="0">
                    <span>${getTranslation('chain.clinics')}</span>
                </div>
                <div class="size-category">
                    <label>${getTranslation('chain.largeHospital')}</label>
                    <input type="number" class="number-input" id="largeHospitals" min="0" value="0">
                    <span>${getTranslation('chain.clinics')}</span>
                </div>
                <div class="service-matrix">
                    <h4>${getTranslation('chain.patientStructure')}</h4>
                    <div class="service-row">
                        <label>${getTranslation('chain.privatePatientShare')}</label>
                        <input type="number" id="chainPrivatePatients" min="0" max="100" value="15" class="number-input">
                        <span>%</span>
                    </div>
                    <div class="service-row">
                        <label>${getTranslation('chain.premiumMealOption')}</label>
                        <input type="number" id="hospitalsPremium" min="0" class="number-input">
                        <span>${getTranslation('chain.of')} <span id="totalHospitalCount">0</span> ${getTranslation('chain.clinics')}</span>
                    </div>
                    <div class="service-row">
                        <label>${getTranslation('chain.staffCateringAvailable')}</label>
                        <input type="number" id="hospitalsStaff" min="0" class="number-input">
                        <span>${getTranslation('chain.of')} <span class="totalHospitalCount">0</span> ${getTranslation('chain.clinics')}</span>
                    </div>
                </div>
            `;
            
            setTimeout(() => {
                ['smallHospitals', 'mediumHospitals', 'largeHospitals'].forEach(id => {
                    document.getElementById(id).addEventListener('input', updateHospitalCount);
                });
            }, 100);
            break;

        case 'betriebsrestaurant':
            container.innerHTML = `
                <h4>${getTranslation('chain.sizeCategories')}</h4>
                <div class="size-category">
                    <label>${getTranslation('chain.smallCanteen')}</label>
                    <input type="number" class="number-input" id="smallCanteens" min="0" value="0">
                    <span>${getTranslation('chain.locations')}</span>
                </div>
                <div class="size-category">
                    <label>${getTranslation('chain.mediumCanteen')}</label>
                    <input type="number" class="number-input" id="mediumCanteens" min="0" value="0">
                    <span>${getTranslation('chain.locations')}</span>
                </div>
                <div class="size-category">
                    <label>${getTranslation('chain.largeCanteen')}</label>
                    <input type="number" class="number-input" id="largeCanteens" min="0" value="0">
                    <span>${getTranslation('chain.locations')}</span>
                </div>
                <div class="service-matrix">
                    <h4>${getTranslation('chain.serviceScope')}</h4>
                    <div class="service-row">
                        <label>${getTranslation('chain.breakfast')}</label>
                        <input type="number" id="canteensBreakfast" min="0" class="number-input">
                        <span>${getTranslation('chain.of')} <span id="totalCanteenCount">0</span> ${getTranslation('chain.locations')}</span>
                    </div>
                    <div class="service-row">
                        <label>${getTranslation('chain.lunch')}</label>
                        <input type="number" id="canteensLunch" min="0" class="number-input">
                        <span>${getTranslation('chain.of')} <span class="totalCanteenCount">0</span> ${getTranslation('chain.locations')}</span>
                    </div>
                    <div class="service-row">
                        <label>${getTranslation('chain.dinner')}</label>
                        <input type="number" id="canteensDinner" min="0" class="number-input">
                        <span>${getTranslation('chain.of')} <span class="totalCanteenCount">0</span> ${getTranslation('chain.locations')}</span>
                    </div>
                </div>
            `;
            
            setTimeout(() => {
                ['smallCanteens', 'mediumCanteens', 'largeCanteens'].forEach(id => {
                    document.getElementById(id).addEventListener('input', updateCanteenCount);
                });
            }, 100);
            break;
    }
}

// Chain Count Update Functions
function updateHotelCount() {
    const small = parseInt(document.getElementById('smallHotels').value) || 0;
    const medium = parseInt(document.getElementById('mediumHotels').value) || 0;
    const large = parseInt(document.getElementById('largeHotels').value) || 0;
    const total = small + medium + large;
    
    document.querySelectorAll('#totalHotelsCount, .totalHotelsCount').forEach(el => {
        el.textContent = total;
    });
    
    // Update total locations
    document.getElementById('totalLocations').value = total;
}

function updateNursingCount() {
    const small = parseInt(document.getElementById('smallNursing').value) || 0;
    const medium = parseInt(document.getElementById('mediumNursing').value) || 0;
    const large = parseInt(document.getElementById('largeNursing').value) || 0;
    const total = small + medium + large;
    
    document.querySelectorAll('#totalNursingCount, .totalNursingCount').forEach(el => {
        el.textContent = total;
    });
    
    document.getElementById('totalLocations').value = total;
}

function updateHospitalCount() {
    const small = parseInt(document.getElementById('smallHospitals').value) || 0;
    const medium = parseInt(document.getElementById('mediumHospitals').value) || 0;
    const large = parseInt(document.getElementById('largeHospitals').value) || 0;
    const total = small + medium + large;
    
    document.querySelectorAll('#totalHospitalCount, .totalHospitalCount').forEach(el => {
        el.textContent = total;
    });
    
    document.getElementById('totalLocations').value = total;
}

function updateCanteenCount() {
    const small = parseInt(document.getElementById('smallCanteens').value) || 0;
    const medium = parseInt(document.getElementById('mediumCanteens').value) || 0;
    const large = parseInt(document.getElementById('largeCanteens').value) || 0;
    const total = small + medium + large;
    
    document.querySelectorAll('#totalCanteenCount, .totalCanteenCount').forEach(el => {
        el.textContent = total;
    });
    
    document.getElementById('totalLocations').value = total;
}

// Update chain details
function updateChainDetails() {
    const totalLocations = parseInt(document.getElementById('totalLocations').value) || 0;
    const industry = document.getElementById('industry').value;
    const detail = document.getElementById('chainLocationsDetail');
    
    if (totalLocations === 0) {
        detail.innerHTML = '<p>Bitte geben Sie die Anzahl der Standorte ein.</p>';
        return;
    }
    
    // Generate location details based on industry
    let locationHTML = '';
    for (let i = 1; i <= totalLocations; i++) {
        locationHTML += `
            <div class="location-item">
                <div class="location-header" onclick="toggleLocationDetail(${i})">
                    <h4>Standort ${i}</h4>
                    <span>▼</span>
                </div>
                <div id="location-detail-${i}" class="location-details">
                    <div class="form-row">
                        <div class="form-group">
                            <label>Standortname</label>
                            <input type="text" id="location-name-${i}" placeholder="z.B. ${industry === 'hotel' ? 'Hotel Berlin' : 'Standort Berlin'}">
                        </div>
                        <div class="form-group">
                            <label>PLZ</label>
                            <input type="text" id="location-zip-${i}" maxlength="5">
                        </div>
                    </div>
                    ${getIndustrySpecificLocationFields(industry, i)}
                </div>
            </div>
        `;
    }
    
    detail.innerHTML = locationHTML;
}

// Get industry specific location fields
function getIndustrySpecificLocationFields(industry, index) {
    switch(industry) {
        case 'hotel':
            return `
                <div class="form-row">
                    <div class="form-group">
                        <label>Zimmer</label>
                        <input type="number" id="location-rooms-${index}" min="1">
                    </div>
                    <div class="form-group">
                        <label>Sterne</label>
                        <select id="location-stars-${index}">
                            <option value="3">3 Sterne</option>
                            <option value="4">4 Sterne</option>
                            <option value="5">5 Sterne</option>
                        </select>
                    </div>
                </div>
            `;
        case 'altenheim':
            return `
                <div class="form-row">
                    <div class="form-group">
                        <label>Bewohner</label>
                        <input type="number" id="location-residents-${index}" min="1">
                    </div>
                    <div class="form-group">
                        <label>Pflegegrade</label>
                        <select id="location-care-${index}">
                            <option value="mixed">Gemischt</option>
                            <option value="high">Schwerpunkt 3-5</option>
                        </select>
                    </div>
                </div>
            `;
        case 'krankenhaus':
            return `
                <div class="form-row">
                    <div class="form-group">
                        <label>Betten</label>
                        <input type="number" id="location-beds-${index}" min="1">
                    </div>
                    <div class="form-group">
                        <label>Fachrichtung</label>
                        <input type="text" id="location-specialty-${index}" placeholder="z.B. Kardiologie">
                    </div>
                </div>
            `;
        case 'betriebsrestaurant':
            return `
                <div class="form-row">
                    <div class="form-group">
                        <label>Mitarbeiter</label>
                        <input type="number" id="location-employees-${index}" min="1">
                    </div>
                    <div class="form-group">
                        <label>Sitzplätze</label>
                        <input type="number" id="location-seats-${index}" min="1">
                    </div>
                </div>
            `;
        default:
            return '';
    }
}

// Save customer data
function saveCustomerData() {
    // Collect all form data
    FreshPlan.state.customerData = {
        // Basic data
        companyName: document.getElementById('companyName').value,
        legalForm: document.getElementById('legalForm').value,
        customerType: document.querySelector('.type-option.selected').getAttribute('data-type'),
        
        // Contact
        contactName: document.getElementById('contactName').value,
        contactPosition: document.getElementById('contactPosition').value,
        contactEmail: document.getElementById('contactEmail').value,
        contactPhone: document.getElementById('contactPhone').value,
        
        // Address
        street: document.getElementById('street').value,
        zipCode: document.getElementById('zipCode').value,
        city: document.getElementById('city').value,
        
        // Business
        industry: document.getElementById('industry').value,
        targetRevenue: parseFloat(document.getElementById('targetRevenue').value),
        
        // Vending
        vendingInterest: document.getElementById('vendingInterest').checked,
        vendingLocations: document.getElementById('vendingLocations')?.value || 0,
        vendingDaily: document.getElementById('vendingDaily')?.value || 0,
        
        // Timestamp
        savedAt: new Date().toISOString()
    };
    
    // Collect industry-specific data
    collectIndustrySpecificData();
    
    // Collect chain data if applicable
    if (FreshPlan.state.customerData.customerType === 'chain') {
        collectChainData();
    }
    
    // Save to storage
    FreshPlan.storage.setItem(FreshPlan.config.storageKeys.customerData, JSON.stringify(FreshPlan.state.customerData));
    
    // Update UI
    showMessage(getTranslation('messages.customerSaved'), 'success');
    FreshPlan.state.tabProgress.customer = true;
    updateProgress();
    
    // Clear autosave
    FreshPlan.storage.removeItem(FreshPlan.config.storageKeys.autosave);
}

// Collect industry specific data
function collectIndustrySpecificData() {
    const industry = FreshPlan.state.customerData.industry;
    
    switch(industry) {
        case 'hotel':
            FreshPlan.state.customerData.hotelRooms = document.getElementById('hotelRooms')?.value || 0;
            FreshPlan.state.customerData.restaurantSeats = document.getElementById('restaurantSeats')?.value || 0;
            FreshPlan.state.customerData.occupancyRate = document.getElementById('occupancyRate')?.value || 75;
            FreshPlan.state.customerData.breakfastGuests = document.getElementById('breakfastGuests')?.value || 0;
            break;
            
        case 'altenheim':
            FreshPlan.state.customerData.residents = document.getElementById('residents')?.value || 0;
            FreshPlan.state.customerData.mealsPerDay = document.getElementById('mealsPerDay')?.value || 3;
            FreshPlan.state.customerData.specialDiets = document.getElementById('specialDiets')?.value || 30;
            FreshPlan.state.customerData.consistencyLevels = document.getElementById('consistencyLevels')?.value || 'no';
            break;
            
        case 'krankenhaus':
            FreshPlan.state.customerData.hospitalBeds = document.getElementById('hospitalBeds')?.value || 0;
            FreshPlan.state.customerData.bedOccupancy = document.getElementById('bedOccupancy')?.value || 85;
            FreshPlan.state.customerData.privatePatientsRatio = document.getElementById('privatePatientsRatio')?.value || 15;
            FreshPlan.state.customerData.premiumMeals = document.getElementById('premiumMeals')?.value || 'no';
            FreshPlan.state.customerData.staffCatering = document.getElementById('staffCatering')?.value || 'no';
            FreshPlan.state.customerData.publicCafeteria = document.getElementById('publicCafeteria')?.value || 'no';
            break;
            
        case 'betriebsrestaurant':
            FreshPlan.state.customerData.employees = document.getElementById('employees')?.value || 0;
            FreshPlan.state.customerData.dailyDiners = document.getElementById('dailyDiners')?.value || 0;
            FreshPlan.state.customerData.operatingDays = document.getElementById('operatingDays')?.value || 250;
            FreshPlan.state.customerData.subsidized = document.getElementById('subsidized')?.value || 'no';
            break;
            
        case 'restaurant':
            FreshPlan.state.customerData.seats = document.getElementById('seats')?.value || 0;
            FreshPlan.state.customerData.dailyGuests = document.getElementById('dailyGuests')?.value || 0;
            FreshPlan.state.customerData.eventCatering = document.getElementById('eventCatering')?.value || 'no';
            FreshPlan.state.customerData.branches = document.getElementById('branches')?.value || 1;
            break;
    }
}

// Collect chain data
function collectChainData() {
    FreshPlan.state.customerData.totalLocations = document.getElementById('totalLocations')?.value || 0;
    FreshPlan.state.customerData.chainData = {};
    
    const industry = FreshPlan.state.customerData.industry;
    
    switch(industry) {
        case 'hotel':
            FreshPlan.state.customerData.chainData = {
                small: document.getElementById('smallHotels')?.value || 0,
                medium: document.getElementById('mediumHotels')?.value || 0,
                large: document.getElementById('largeHotels')?.value || 0,
                services: {
                    breakfast: document.getElementById('hotelsBreakfast')?.value || 0,
                    restaurant: document.getElementById('hotelsRestaurant')?.value || 0,
                    roomService: document.getElementById('hotelsRoomService')?.value || 0,
                    banquet: document.getElementById('hotelsBanquet')?.value || 0
                }
            };
            break;
            
        case 'altenheim':
            FreshPlan.state.customerData.chainData = {
                small: document.getElementById('smallNursing')?.value || 0,
                medium: document.getElementById('mediumNursing')?.value || 0,
                large: document.getElementById('largeNursing')?.value || 0,
                services: {
                    fullService: document.getElementById('nursingFullService')?.value || 0,
                    snacks: document.getElementById('nursingSnacks')?.value || 0
                }
            };
            break;
            
        case 'krankenhaus':
            FreshPlan.state.customerData.chainData = {
                small: document.getElementById('smallHospitals')?.value || 0,
                medium: document.getElementById('mediumHospitals')?.value || 0,
                large: document.getElementById('largeHospitals')?.value || 0,
                privatePatients: document.getElementById('chainPrivatePatients')?.value || 15,
                services: {
                    premium: document.getElementById('hospitalsPremium')?.value || 0,
                    staff: document.getElementById('hospitalsStaff')?.value || 0
                }
            };
            break;
            
        case 'betriebsrestaurant':
            FreshPlan.state.customerData.chainData = {
                small: document.getElementById('smallCanteens')?.value || 0,
                medium: document.getElementById('mediumCanteens')?.value || 0,
                large: document.getElementById('largeCanteens')?.value || 0,
                services: {
                    breakfast: document.getElementById('canteensBreakfast')?.value || 0,
                    lunch: document.getElementById('canteensLunch')?.value || 0,
                    dinner: document.getElementById('canteensDinner')?.value || 0
                }
            };
            break;
    }
}

// Revenue potential calculator
function calculatePotential() {
    const industry = document.getElementById('industry').value;
    
    if (!industry) {
        showMessage(getTranslation('messages.enterBranchData'), 'error');
        return;
    }
    
    let potential = {};
    
    switch(industry) {
        case 'hotel':
            potential = calculateHotelPotential();
            break;
        case 'altenheim':
            potential = calculateNursingPotential();
            break;
        case 'krankenhaus':
            potential = calculateHospitalPotential();
            break;
        case 'betriebsrestaurant':
            potential = calculateCanteenPotential();
            break;
        case 'restaurant':
            potential = calculateRestaurantPotential();
            break;
    }
    
    showCalculationModal(potential);
}

// Calculate hotel potential
function calculateHotelPotential() {
    const rooms = parseInt(document.getElementById('hotelRooms')?.value) || 0;
    const occupancy = (parseInt(document.getElementById('occupancyRate')?.value) || 75) / 100;
    const restaurantSeats = parseInt(document.getElementById('restaurantSeats')?.value) || 0;
    const breakfastGuests = parseInt(document.getElementById('breakfastGuests')?.value) || 0;
    
    const industryConfig = FreshPlan.config.industries.hotel;
    
    const breakdown = {
        roomService: rooms * occupancy * 365 * industryConfig.utilization.roomService * industryConfig.avgPrices.roomService,
        breakfast: breakfastGuests * 365 * industryConfig.avgPrices.breakfast,
        restaurant: restaurantSeats * industryConfig.utilization.restaurant * 365 * industryConfig.avgPrices.restaurant
    };
    
    const total = Object.values(breakdown).reduce((a, b) => a + b, 0);
    
    return {
        total: Math.round(total),
        breakdown: breakdown,
        realistic: Math.round(total * 0.3) // 30% market share
    };
}

// Calculate nursing home potential
function calculateNursingPotential() {
    const residents = parseInt(document.getElementById('residents')?.value) || 0;
    const mealsPerDay = parseInt(document.getElementById('mealsPerDay')?.value) || 3;
    
    const industryConfig = FreshPlan.config.industries.altenheim;
    
    const breakdown = {
        standard: residents * industryConfig.ratios.standard * mealsPerDay * 365 * industryConfig.avgPrices.standard,
        special: residents * industryConfig.ratios.special * mealsPerDay * 365 * industryConfig.avgPrices.special
    };
    
    const total = Object.values(breakdown).reduce((a, b) => a + b, 0);
    
    return {
        total: Math.round(total),
        breakdown: breakdown,
        realistic: Math.round(total * 0.4) // 40% market share
    };
}

// Calculate hospital potential
function calculateHospitalPotential() {
    const beds = parseInt(document.getElementById('hospitalBeds')?.value) || 0;
    const occupancy = (parseInt(document.getElementById('bedOccupancy')?.value) || 85) / 100;
    const privateRatio = (parseInt(document.getElementById('privatePatientsRatio')?.value) || 15) / 100;
    const staffCatering = document.getElementById('staffCatering')?.value === 'yes';
    
    const industryConfig = FreshPlan.config.industries.krankenhaus;
    
    const breakdown = {
        standard: beds * occupancy * (1 - privateRatio) * 365 * industryConfig.avgPrices.standard,
        private: beds * occupancy * privateRatio * 365 * industryConfig.avgPrices.private,
        staff: staffCatering ? beds * industryConfig.staffRatio * 250 * industryConfig.avgPrices.staff : 0
    };
    
    const total = Object.values(breakdown).reduce((a, b) => a + b, 0);
    
    return {
        total: Math.round(total),
        breakdown: breakdown,
        realistic: Math.round(total * 0.35) // 35% market share
    };
}

// Calculate canteen potential
function calculateCanteenPotential() {
    const employees = parseInt(document.getElementById('employees')?.value) || 0;
    const dailyDiners = parseInt(document.getElementById('dailyDiners')?.value) || 0;
    const operatingDays = parseInt(document.getElementById('operatingDays')?.value) || 250;
    
    const industryConfig = FreshPlan.config.industries.betriebsrestaurant;
    const utilization = dailyDiners / employees;
    
    const breakdown = {
        regular: dailyDiners * operatingDays * industryConfig.avgPrice
    };
    
    const total = breakdown.regular;
    
    return {
        total: Math.round(total),
        breakdown: breakdown,
        realistic: Math.round(total * 0.5), // 50% market share
        utilization: Math.round(utilization * 100)
    };
}

// Calculate restaurant potential
function calculateRestaurantPotential() {
    const seats = parseInt(document.getElementById('seats')?.value) || 0;
    const dailyGuests = parseInt(document.getElementById('dailyGuests')?.value) || 0;
    const eventCatering = document.getElementById('eventCatering')?.value === 'yes';
    
    const industryConfig = FreshPlan.config.industries.restaurant;
    
    const breakdown = {
        regular: dailyGuests * industryConfig.operatingDays * industryConfig.avgPrices.regular,
        events: eventCatering ? seats * industryConfig.eventsPerYear * industryConfig.avgPrices.event : 0
    };
    
    const total = Object.values(breakdown).reduce((a, b) => a + b, 0);
    
    return {
        total: Math.round(total),
        breakdown: breakdown,
        realistic: Math.round(total * 0.25) // 25% market share
    };
}

// Show calculation modal
function showCalculationModal(result) {
    const modal = document.getElementById('calcModal');
    const content = document.getElementById('calcContent');
    const industry = document.getElementById('industry').value;
    
    let html = `<h3>${getTranslation('calc.' + industry + 'Calc')}</h3>`;
    
    // Show breakdown
    html += '<div class="calc-breakdown">';
    for (const [key, value] of Object.entries(result.breakdown)) {
        if (value > 0) {
            html += `
                <div class="calc-line">
                    <span>${getTranslation('calc.' + key) || key}:</span>
                    <span>${formatCurrency(value)}</span>
                </div>
            `;
        }
    }
    html += '</div>';
    
    // Show totals
    html += `
        <div class="calc-breakdown" style="margin-top: 1rem; border-top: 2px solid white; padding-top: 1rem;">
            <div class="calc-line" style="font-size: 1.2rem;">
                <span>${getTranslation('calc.totalPotential')}:</span>
                <span>${formatCurrency(result.total)}</span>
            </div>
            <div class="calc-line" style="font-size: 1.5rem; font-weight: bold;">
                <span>${getTranslation('calc.realisticTarget')}:</span>
                <span>${formatCurrency(result.realistic)}</span>
            </div>
        </div>
    `;
    
    // Show utilization for canteens
    if (result.utilization !== undefined) {
        html += `
            <div class="info-box" style="margin-top: 1rem;">
                <p>${getTranslation('calc.utilization')}: ${result.utilization}% ${getTranslation('calc.ofEmployees')}</p>
            </div>
        `;
    }
    
    content.innerHTML = html;
    modal.style.display = 'block';
    
    // Store calculated revenue
    FreshPlan.state.calculatedRevenue = result.realistic;
}

// Export functions
window.initializeCustomerForm = initializeCustomerForm;
window.initializeValidation = initializeValidation;
window.initializeAutosave = initializeAutosave;
window.updateBranchFields = updateBranchFields;
window.saveCustomerData = saveCustomerData;
window.calculatePotential = calculatePotential;
window.updateHotelCount = updateHotelCount;
window.updateNursingCount = updateNursingCount;
window.updateHospitalCount = updateHospitalCount;
window.updateCanteenCount = updateCanteenCount;
window.updateChainDetails = updateChainDetails;