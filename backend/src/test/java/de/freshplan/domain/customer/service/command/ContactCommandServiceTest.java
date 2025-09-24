package de.freshplan.domain.customer.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.ContactDTO;
import de.freshplan.domain.customer.service.mapper.ContactMapper;
import de.freshplan.test.builders.ContactTestDataFactory;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ContactCommandService. Verifies that the service behaves EXACTLY like
 * ContactService for command operations.
 */
@QuarkusTest
@Tag("core")
class ContactCommandServiceTest {

  @Inject ContactCommandService commandService;

  // CustomerBuilder for unit tests (without CDI)
  private final CustomerBuilder customerBuilder = new CustomerBuilder();

  @InjectMock ContactRepository contactRepository;

  @InjectMock CustomerRepository customerRepository;

  @InjectMock ContactMapper contactMapper;

  @InjectMock SecurityIdentity securityIdentity;

  private Customer mockCustomer;
  private CustomerContact mockContact;
  private ContactDTO mockContactDTO;
  private Principal mockPrincipal;

  @BeforeEach
  void setUp() {
    // Setup mock customer using CustomerBuilder directly
    mockCustomer =
        customerBuilder
            .withCompanyName("Test Company GmbH")
            .build(); // Using build() for unit test (no DB)
    mockCustomer.setId(UUID.randomUUID());

    // Setup mock contact using ContactTestDataFactory
    mockContact =
        ContactTestDataFactory.builder()
            .forCustomer(mockCustomer)
            .withFirstName("Max")
            .withLastName("Mustermann")
            .withEmail("max@test.de")
            .build();
    mockContact.setId(UUID.randomUUID());
    mockContact.setIsPrimary(false);
    mockContact.setIsActive(true);

    // Setup mock DTO
    mockContactDTO = new ContactDTO();
    mockContactDTO.setId(mockContact.getId());
    mockContactDTO.setFirstName("Max");
    mockContactDTO.setLastName("Mustermann");
    mockContactDTO.setEmail("max@test.de");

    // Setup security mock
    mockPrincipal = mock(Principal.class);
    when(mockPrincipal.getName()).thenReturn("testuser");
    when(securityIdentity.getPrincipal()).thenReturn(mockPrincipal);
  }

  @Test
  void createContact_withValidData_shouldCreateContact() {
    // Given
    UUID customerId = mockCustomer.getId();
    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.of(mockCustomer));
    when(contactMapper.toEntity(mockContactDTO)).thenReturn(mockContact);
    when(contactRepository.hasContacts(customerId)).thenReturn(false); // First contact
    when(contactMapper.toDTO(mockContact)).thenReturn(mockContactDTO);

    // When
    ContactDTO result = commandService.createContact(customerId, mockContactDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getFirstName()).isEqualTo("Max");

    // Verify first contact becomes primary
    assertThat(mockContact.getIsPrimary()).isTrue();

    // Verify audit fields
    verify(contactRepository).persist(mockContact);
    assertThat(mockContact.getCreatedBy()).isEqualTo("testuser");
    assertThat(mockContact.getUpdatedBy()).isEqualTo("testuser");
  }

  @Test
  void createContact_whenCustomerNotFound_shouldThrowNotFoundException() {
    // Given
    UUID customerId = UUID.randomUUID();
    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> commandService.createContact(customerId, mockContactDTO))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Customer not found");
  }

  @Test
  void createContact_whenNotFirstContact_shouldNotSetPrimary() {
    // Given
    UUID customerId = mockCustomer.getId();
    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.of(mockCustomer));
    when(contactMapper.toEntity(mockContactDTO)).thenReturn(mockContact);
    when(contactRepository.hasContacts(customerId)).thenReturn(true); // Not first contact
    when(contactMapper.toDTO(mockContact)).thenReturn(mockContactDTO);

    // When
    ContactDTO result = commandService.createContact(customerId, mockContactDTO);

    // Then
    assertThat(mockContact.getIsPrimary()).isFalse();
    verify(contactRepository).persist(mockContact);
  }

  @Test
  void updateContact_withValidData_shouldUpdateContact() {
    // Given
    UUID contactId = mockContact.getId();
    ContactDTO updateDTO = new ContactDTO();
    updateDTO.setFirstName("Updated");
    updateDTO.setLastName("Name");

    when(contactRepository.findByIdOptional(contactId)).thenReturn(Optional.of(mockContact));
    when(contactMapper.toDTO(mockContact)).thenReturn(updateDTO);

    // When
    ContactDTO result = commandService.updateContact(contactId, updateDTO);

    // Then
    assertThat(result).isNotNull();
    verify(contactMapper).updateEntityFromDTO(updateDTO, mockContact);
    assertThat(mockContact.getUpdatedBy()).isEqualTo("testuser");
  }

  @Test
  void updateContact_withCustomerIdVerification_shouldUpdateContact() {
    // Given
    UUID customerId = mockCustomer.getId();
    UUID contactId = mockContact.getId();
    ContactDTO updateDTO = new ContactDTO();
    updateDTO.setFirstName("Updated");

    when(contactRepository.findByIdOptional(contactId)).thenReturn(Optional.of(mockContact));
    when(contactMapper.toDTO(mockContact)).thenReturn(updateDTO);

    // When
    ContactDTO result = commandService.updateContact(customerId, contactId, updateDTO);

    // Then
    assertThat(result).isNotNull();
    verify(contactMapper).updateEntityFromDTO(updateDTO, mockContact);
  }

  @Test
  void updateContact_whenContactNotBelongsToCustomer_shouldThrowNotFoundException() {
    // Given
    UUID wrongCustomerId = UUID.randomUUID();
    UUID contactId = mockContact.getId();

    when(contactRepository.findByIdOptional(contactId)).thenReturn(Optional.of(mockContact));

    // When/Then
    assertThatThrownBy(
            () -> commandService.updateContact(wrongCustomerId, contactId, mockContactDTO))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Contact not found");
  }

  @Test
  void setPrimaryContact_withValidData_shouldSetPrimary() {
    // Given
    UUID customerId = mockCustomer.getId();
    UUID contactId = mockContact.getId();

    when(contactRepository.findByIdOptional(contactId)).thenReturn(Optional.of(mockContact));

    // When
    commandService.setPrimaryContact(customerId, contactId);

    // Then
    verify(contactRepository).setPrimaryContact(customerId, contactId);
  }

  @Test
  void setPrimaryContact_whenContactNotBelongsToCustomer_shouldThrowException() {
    // Given
    UUID wrongCustomerId = UUID.randomUUID();
    UUID contactId = mockContact.getId();

    when(contactRepository.findByIdOptional(contactId)).thenReturn(Optional.of(mockContact));

    // When/Then
    assertThatThrownBy(() -> commandService.setPrimaryContact(wrongCustomerId, contactId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Contact does not belong to the specified customer");
  }

  @Test
  void deleteContact_whenNotPrimary_shouldSoftDelete() {
    // Given
    UUID contactId = mockContact.getId();
    mockContact.setIsPrimary(false);

    when(contactRepository.findByIdOptional(contactId)).thenReturn(Optional.of(mockContact));

    // When
    commandService.deleteContact(contactId);

    // Then
    assertThat(mockContact.getIsActive()).isFalse();
    assertThat(mockContact.getUpdatedBy()).isEqualTo("testuser");
    verify(contactRepository, never()).countActiveByCustomerId(any());
  }

  @Test
  void deleteContact_whenPrimaryAndOnlyContact_shouldSoftDelete() {
    // Given
    UUID contactId = mockContact.getId();
    mockContact.setIsPrimary(true);

    when(contactRepository.findByIdOptional(contactId)).thenReturn(Optional.of(mockContact));
    when(contactRepository.countActiveByCustomerId(mockCustomer.getId()))
        .thenReturn(1L); // Only one contact

    // When
    commandService.deleteContact(contactId);

    // Then
    assertThat(mockContact.getIsActive()).isFalse();
    assertThat(mockContact.getUpdatedBy()).isEqualTo("testuser");
  }

  @Test
  void deleteContact_whenPrimaryAndOtherContactsExist_shouldThrowException() {
    // Given
    UUID contactId = mockContact.getId();
    mockContact.setIsPrimary(true);

    when(contactRepository.findByIdOptional(contactId)).thenReturn(Optional.of(mockContact));
    when(contactRepository.countActiveByCustomerId(mockCustomer.getId()))
        .thenReturn(2L); // Multiple contacts

    // When/Then
    assertThatThrownBy(() -> commandService.deleteContact(contactId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot delete primary contact");
  }

  @Test
  void deleteContact_withCustomerIdVerification_shouldSoftDelete() {
    // Given
    UUID customerId = mockCustomer.getId();
    UUID contactId = mockContact.getId();
    mockContact.setIsPrimary(false);

    when(contactRepository.findByIdOptional(contactId)).thenReturn(Optional.of(mockContact));

    // When
    commandService.deleteContact(customerId, contactId);

    // Then
    assertThat(mockContact.getIsActive()).isFalse();
  }

  @Test
  void assignContactsToLocation_shouldUpdateLocation() {
    // Given
    List<UUID> contactIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
    UUID locationId = UUID.randomUUID();

    when(contactRepository.updateLocationAssignment(contactIds, locationId)).thenReturn(2);

    // When
    int result = commandService.assignContactsToLocation(contactIds, locationId);

    // Then
    assertThat(result).isEqualTo(2);
    verify(contactRepository).updateLocationAssignment(contactIds, locationId);
  }

  @Test
  void getCurrentUser_whenSecurityIdentityAvailable_shouldReturnUsername() {
    // This is tested indirectly through createContact test
    // The getCurrentUser method is private and tested via public methods
    // We verify it worked correctly by checking the audit fields in createContact tests

    // Given
    UUID customerId = mockCustomer.getId();
    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.of(mockCustomer));
    when(contactMapper.toEntity(mockContactDTO)).thenReturn(mockContact);
    when(contactRepository.hasContacts(customerId)).thenReturn(false);
    when(contactMapper.toDTO(mockContact)).thenReturn(mockContactDTO);

    // When
    commandService.createContact(customerId, mockContactDTO);

    // Then - verify audit fields were set
    assertThat(mockContact.getCreatedBy()).isEqualTo("testuser");
    assertThat(mockContact.getUpdatedBy()).isEqualTo("testuser");
  }

  @Test
  void getCurrentUser_whenSecurityIdentityThrowsException_shouldUseFallback() {
    // Given
    when(securityIdentity.getPrincipal()).thenThrow(new RuntimeException("No auth"));

    // Set test.user system property for fallback
    System.setProperty("test.user", "fallback-user");

    UUID customerId = mockCustomer.getId();
    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.of(mockCustomer));
    when(contactMapper.toEntity(mockContactDTO)).thenReturn(mockContact);
    when(contactRepository.hasContacts(customerId)).thenReturn(false);
    when(contactMapper.toDTO(mockContact)).thenReturn(mockContactDTO);

    // When
    ContactDTO result = commandService.createContact(customerId, mockContactDTO);

    // Then
    assertThat(mockContact.getCreatedBy()).isEqualTo("fallback-user");

    // Cleanup
    System.clearProperty("test.user");
  }
}
