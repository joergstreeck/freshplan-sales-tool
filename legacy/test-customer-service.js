// Manual test for CustomerServiceV2 and Repository
import { LocalStorageCustomerRepository } from './src/infrastructure/repositories/LocalStorageCustomerRepository.js';
import { CustomerServiceV2 } from './src/services/CustomerServiceV2.js';

console.log('=== CustomerServiceV2 Manual Test ===\n');

async function runTest() {
    try {
        // 1. Test Repository
        console.log('1. Testing LocalStorageCustomerRepository...');
        const repository = new LocalStorageCustomerRepository();
        console.log('✓ Repository created');
        
        // 2. Test Service Creation
        console.log('\n2. Testing CustomerServiceV2...');
        const service = new CustomerServiceV2(repository);
        console.log('✓ Service created');
        
        // 3. Test Load Initial Data
        console.log('\n3. Testing loadInitialCustomerData...');
        const initialData = await service.loadInitialCustomerData();
        console.log('✓ Initial data:', initialData ? 'Found' : 'None');
        
        // 4. Test Save Valid Data
        console.log('\n4. Testing saveCurrentCustomerData with valid data...');
        const validData = {
            companyName: 'Test GmbH',
            legalForm: 'gmbh',
            customerType: 'single',
            customerStatus: 'neukunde',
            industry: 'hotel',
            street: 'Teststraße 1',
            postalCode: '12345',
            city: 'Berlin',
            contactName: 'Max Mustermann',
            contactPhone: '0123456789',
            contactEmail: 'test@example.com',
            expectedVolume: '50000',
            paymentMethod: 'vorkasse'
        };
        
        await service.saveCurrentCustomerData(validData);
        console.log('✓ Valid data saved successfully');
        
        // 5. Test Load After Save
        console.log('\n5. Testing data persistence...');
        const loadedData = await service.loadInitialCustomerData();
        console.log('✓ Loaded data company:', loadedData?.companyName);
        
        // 6. Test Invalid Data
        console.log('\n6. Testing validation with invalid data...');
        try {
            await service.saveCurrentCustomerData({ companyName: '' });
            console.log('✗ Should have thrown validation error');
        } catch (error) {
            console.log('✓ Validation error thrown:', error.message);
            console.log('  Error count:', error.errors?.length);
        }
        
        // 7. Test Payment Warning
        console.log('\n7. Testing payment warning logic...');
        const shouldWarn1 = service.shouldShowPaymentWarning('neukunde', 'rechnung');
        const shouldWarn2 = service.shouldShowPaymentWarning('bestandskunde', 'rechnung');
        console.log('✓ Neukunde + Rechnung warning:', shouldWarn1);
        console.log('✓ Bestandskunde + Rechnung warning:', shouldWarn2);
        
        // 8. Test Clear
        console.log('\n8. Testing clearAllCustomerData...');
        await service.clearAllCustomerData();
        const afterClear = await service.loadInitialCustomerData();
        console.log('✓ Data after clear:', afterClear ? 'Still exists' : 'Cleared');
        
        // 9. Check localStorage structure
        console.log('\n9. Checking localStorage structure...');
        const rawData = localStorage.getItem('freshplanData');
        console.log('✓ Raw localStorage data:', rawData ? JSON.parse(rawData) : 'Empty');
        
        console.log('\n=== All tests completed ===');
        
    } catch (error) {
        console.error('\n✗ Test failed:', error);
        console.error(error.stack);
    }
}

// Run the test
runTest();