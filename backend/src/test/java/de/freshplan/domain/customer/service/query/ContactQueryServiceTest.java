package de.freshplan.domain.customer.service.query;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.service.dto.ContactDTO;
import de.freshplan.domain.customer.service.mapper.ContactMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContactQueryService.
 * Verifies that the service behaves EXACTLY like ContactService for query operations.
 * Note: No @Transactional annotation should be present on query methods!
 */
@QuarkusTest
class ContactQueryServiceTest {
    
    @Inject
    ContactQueryService queryService;
    
    @InjectMock
    ContactRepository contactRepository;
    
    @InjectMock
    ContactMapper contactMapper;
    
    private Customer mockCustomer;
    private CustomerContact mockContact1;
    private CustomerContact mockContact2;
    private ContactDTO mockContactDTO1;
    private ContactDTO mockContactDTO2;
    
    @BeforeEach
    void setUp() {
        // Setup mock customer
        mockCustomer = new Customer();
        mockCustomer.setId(UUID.randomUUID());
        mockCustomer.setCompanyName("Test Company GmbH");
        
        // Setup first mock contact
        mockContact1 = new CustomerContact();
        mockContact1.setId(UUID.randomUUID());
        mockContact1.setCustomer(mockCustomer);
        mockContact1.setFirstName("Max");
        mockContact1.setLastName("Mustermann");
        mockContact1.setEmail("max@test.de");
        mockContact1.setIsPrimary(true);
        mockContact1.setIsActive(true);
        
        // Setup second mock contact
        mockContact2 = new CustomerContact();
        mockContact2.setId(UUID.randomUUID());
        mockContact2.setCustomer(mockCustomer);
        mockContact2.setFirstName("Erika");
        mockContact2.setLastName("Musterfrau");
        mockContact2.setEmail("erika@test.de");
        mockContact2.setIsPrimary(false);
        mockContact2.setIsActive(true);
        
        // Setup mock DTOs
        mockContactDTO1 = new ContactDTO();
        mockContactDTO1.setId(mockContact1.getId());
        mockContactDTO1.setFirstName("Max");
        mockContactDTO1.setLastName("Mustermann");
        mockContactDTO1.setEmail("max@test.de");
        mockContactDTO1.setPrimary(true);
        
        mockContactDTO2 = new ContactDTO();
        mockContactDTO2.setId(mockContact2.getId());
        mockContactDTO2.setFirstName("Erika");
        mockContactDTO2.setLastName("Musterfrau");
        mockContactDTO2.setEmail("erika@test.de");
        mockContactDTO2.setPrimary(false);
    }
    
    @Test
    void getContactsByCustomerId_shouldReturnAllContacts() {
        // Given
        UUID customerId = mockCustomer.getId();
        List<CustomerContact> contacts = Arrays.asList(mockContact1, mockContact2);
        
        when(contactRepository.findByCustomerId(customerId))
            .thenReturn(contacts);
        when(contactMapper.toDTO(mockContact1))
            .thenReturn(mockContactDTO1);
        when(contactMapper.toDTO(mockContact2))
            .thenReturn(mockContactDTO2);
        
        // When
        List<ContactDTO> result = queryService.getContactsByCustomerId(customerId);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("Max");
        assertThat(result.get(1).getFirstName()).isEqualTo("Erika");
        verify(contactRepository).findByCustomerId(customerId);
    }
    
    @Test
    void getContactsByCustomerId_whenNoContacts_shouldReturnEmptyList() {
        // Given
        UUID customerId = UUID.randomUUID();
        when(contactRepository.findByCustomerId(customerId))
            .thenReturn(Collections.emptyList());
        
        // When
        List<ContactDTO> result = queryService.getContactsByCustomerId(customerId);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void getContact_withValidId_shouldReturnContact() {
        // Given
        UUID contactId = mockContact1.getId();
        when(contactRepository.findByIdOptional(contactId))
            .thenReturn(Optional.of(mockContact1));
        when(contactMapper.toDTO(mockContact1))
            .thenReturn(mockContactDTO1);
        
        // When
        ContactDTO result = queryService.getContact(contactId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Max");
        assertThat(result.isPrimary()).isTrue();
    }
    
    @Test
    void getContact_whenNotFound_shouldThrowNotFoundException() {
        // Given
        UUID contactId = UUID.randomUUID();
        when(contactRepository.findByIdOptional(contactId))
            .thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> queryService.getContact(contactId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Contact not found: " + contactId);
    }
    
    @Test
    void getContact_withCustomerIdVerification_shouldReturnContact() {
        // Given
        UUID customerId = mockCustomer.getId();
        UUID contactId = mockContact1.getId();
        
        when(contactRepository.findByIdOptional(contactId))
            .thenReturn(Optional.of(mockContact1));
        when(contactMapper.toDTO(mockContact1))
            .thenReturn(mockContactDTO1);
        
        // When
        ContactDTO result = queryService.getContact(customerId, contactId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Max");
    }
    
    @Test
    void getContact_whenContactNotBelongsToCustomer_shouldThrowNotFoundException() {
        // Given
        UUID wrongCustomerId = UUID.randomUUID();
        UUID contactId = mockContact1.getId();
        
        when(contactRepository.findByIdOptional(contactId))
            .thenReturn(Optional.of(mockContact1));
        
        // When/Then
        assertThatThrownBy(() -> queryService.getContact(wrongCustomerId, contactId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Contact not found: " + contactId);
    }
    
    @Test
    void getContactsByLocationId_shouldReturnContactsForLocation() {
        // Given
        UUID locationId = UUID.randomUUID();
        List<CustomerContact> contacts = Arrays.asList(mockContact1, mockContact2);
        
        when(contactRepository.findByLocationId(locationId))
            .thenReturn(contacts);
        when(contactMapper.toDTO(mockContact1))
            .thenReturn(mockContactDTO1);
        when(contactMapper.toDTO(mockContact2))
            .thenReturn(mockContactDTO2);
        
        // When
        List<ContactDTO> result = queryService.getContactsByLocationId(locationId);
        
        // Then
        assertThat(result).hasSize(2);
        verify(contactRepository).findByLocationId(locationId);
    }
    
    @Test
    void getUpcomingBirthdays_shouldReturnContactsWithBirthdays() {
        // Given
        int daysAhead = 30;
        List<CustomerContact> contacts = Arrays.asList(mockContact1);
        
        when(contactRepository.findUpcomingBirthdays(daysAhead))
            .thenReturn(contacts);
        when(contactMapper.toDTO(mockContact1))
            .thenReturn(mockContactDTO1);
        
        // When
        List<ContactDTO> result = queryService.getUpcomingBirthdays(daysAhead);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Max");
        verify(contactRepository).findUpcomingBirthdays(daysAhead);
    }
    
    @Test
    void isEmailInUse_whenEmailExists_shouldReturnTrue() {
        // Given
        String email = "max@test.de";
        when(contactRepository.findByEmail(email))
            .thenReturn(Arrays.asList(mockContact1));
        
        // When
        boolean result = queryService.isEmailInUse(email, null);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void isEmailInUse_whenEmailNotExists_shouldReturnFalse() {
        // Given
        String email = "new@test.de";
        when(contactRepository.findByEmail(email))
            .thenReturn(Collections.emptyList());
        
        // When
        boolean result = queryService.isEmailInUse(email, null);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void isEmailInUse_withExcludeContactId_shouldExcludeContact() {
        // Given
        String email = "max@test.de";
        UUID excludeId = mockContact1.getId();
        
        when(contactRepository.findByEmail(email))
            .thenReturn(Arrays.asList(mockContact1));
        
        // When - excluding the same contact that has the email
        boolean result = queryService.isEmailInUse(email, excludeId);
        
        // Then
        assertThat(result).isFalse(); // Email not in use by ANOTHER contact
    }
    
    @Test
    void isEmailInUse_withExcludeContactId_whenOtherContactHasEmail_shouldReturnTrue() {
        // Given
        String email = "shared@test.de";
        UUID excludeId = UUID.randomUUID(); // Different ID
        mockContact1.setEmail(email);
        mockContact2.setEmail(email);
        
        when(contactRepository.findByEmail(email))
            .thenReturn(Arrays.asList(mockContact1, mockContact2));
        
        // When - excluding a different contact
        boolean result = queryService.isEmailInUse(email, excludeId);
        
        // Then
        assertThat(result).isTrue(); // Email is in use by other contacts
    }
}