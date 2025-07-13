package de.freshplan.api;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

/**
 * Development data initializer that creates test customers.
 * Only active in dev profile.
 */
@ApplicationScoped
@IfBuildProfile("dev")
public class CustomerDataInitializer {

    private static final Logger LOG = Logger.getLogger(CustomerDataInitializer.class);

    @Inject
    CustomerRepository customerRepository;

    @Transactional
    void onStart(@Observes(notifyObserver = Reception.IF_EXISTS) StartupEvent ev) {
        LOG.info("Initializing customer development data...");

        // Check if we already have customers
        if (customerRepository.count() > 0) {
            LOG.info("Customers already exist, skipping initialization");
            return;
        }

        // Create test customers
        createHotelCustomer();
        createRestaurantCustomer();
        createCateringCustomer();
        createSchoolCustomer();
        createEventCustomer();

        LOG.info("Customer development data initialized successfully");
    }

    private void createHotelCustomer() {
        Customer customer = new Customer();
        customer.setCustomerNumber("KD-2025-00001");
        customer.setCompanyName("Grand Hotel Berlin");
        customer.setTradingName("Grand Hotel");
        customer.setLegalForm("GmbH");
        customer.setCustomerType(CustomerType.UNTERNEHMEN);
        customer.setIndustry(Industry.HOTEL);
        customer.setClassification(Classification.A_KUNDE);
        customer.setHierarchyType(CustomerHierarchyType.STANDALONE);
        customer.setStatus(CustomerStatus.AKTIV);
        customer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
        customer.setExpectedAnnualVolume(new BigDecimal("120000.00"));
        customer.setActualAnnualVolume(new BigDecimal("98000.00"));
        customer.setPaymentTerms(PaymentTerms.NETTO_30);
        customer.setCreditLimit(new BigDecimal("25000.00"));
        customer.setDeliveryCondition(DeliveryCondition.STANDARD);
        customer.setLastContactDate(LocalDateTime.now().minusDays(5));
        customer.setNextFollowUpDate(LocalDateTime.now().plusDays(10));
        customer.setCreatedBy("system");
        
        customerRepository.persist(customer);
        LOG.info("Created hotel customer: " + customer.getCompanyName());
    }

    private void createRestaurantCustomer() {
        Customer customer = new Customer();
        customer.setCustomerNumber("KD-2025-00002");
        customer.setCompanyName("Bella Italia Restaurant");
        customer.setTradingName("Bella Italia");
        customer.setLegalForm("Einzelunternehmen");
        customer.setCustomerType(CustomerType.UNTERNEHMEN);
        customer.setIndustry(Industry.GASTSTAETTE);
        customer.setClassification(Classification.B_KUNDE);
        customer.setHierarchyType(CustomerHierarchyType.STANDALONE);
        customer.setStatus(CustomerStatus.AKTIV);
        customer.setLifecycleStage(CustomerLifecycleStage.RETENTION);
        customer.setExpectedAnnualVolume(new BigDecimal("45000.00"));
        customer.setActualAnnualVolume(new BigDecimal("42000.00"));
        customer.setPaymentTerms(PaymentTerms.NETTO_14);
        customer.setCreditLimit(new BigDecimal("10000.00"));
        customer.setDeliveryCondition(DeliveryCondition.EXPRESS);
        customer.setLastContactDate(LocalDateTime.now().minusDays(2));
        customer.setNextFollowUpDate(LocalDateTime.now().plusDays(7));
        customer.setCreatedBy("system");
        
        customerRepository.persist(customer);
        LOG.info("Created restaurant customer: " + customer.getCompanyName());
    }

    private void createCateringCustomer() {
        Customer customer = new Customer();
        customer.setCustomerNumber("KD-2025-00003");
        customer.setCompanyName("Event Catering München GmbH");
        customer.setTradingName("Event Catering");
        customer.setLegalForm("GmbH");
        customer.setCustomerType(CustomerType.UNTERNEHMEN);
        customer.setIndustry(Industry.CATERING);
        customer.setClassification(Classification.A_KUNDE);
        customer.setHierarchyType(CustomerHierarchyType.HEADQUARTER);
        customer.setStatus(CustomerStatus.AKTIV);
        customer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
        customer.setExpectedAnnualVolume(new BigDecimal("200000.00"));
        customer.setActualAnnualVolume(new BigDecimal("180000.00"));
        customer.setPaymentTerms(PaymentTerms.NETTO_30);
        customer.setCreditLimit(new BigDecimal("50000.00"));
        customer.setDeliveryCondition(DeliveryCondition.STANDARD);
        customer.setLastContactDate(LocalDateTime.now().minusDays(1));
        customer.setNextFollowUpDate(LocalDateTime.now().plusDays(14));
        customer.setCreatedBy("system");
        
        customerRepository.persist(customer);
        LOG.info("Created catering customer: " + customer.getCompanyName());
    }

    private void createSchoolCustomer() {
        Customer customer = new Customer();
        customer.setCustomerNumber("KD-2025-00004");
        customer.setCompanyName("Städtische Grundschule Nord");
        customer.setTradingName("Grundschule Nord");
        customer.setLegalForm("Öffentliche Einrichtung");
        customer.setCustomerType(CustomerType.INSTITUTION);
        customer.setIndustry(Industry.SCHULE);
        customer.setClassification(Classification.C_KUNDE);
        customer.setHierarchyType(CustomerHierarchyType.STANDALONE);
        customer.setStatus(CustomerStatus.AKTIV);
        customer.setLifecycleStage(CustomerLifecycleStage.RETENTION);
        customer.setExpectedAnnualVolume(new BigDecimal("35000.00"));
        customer.setActualAnnualVolume(new BigDecimal("33000.00"));
        customer.setPaymentTerms(PaymentTerms.NETTO_60);
        customer.setCreditLimit(new BigDecimal("15000.00"));
        customer.setDeliveryCondition(DeliveryCondition.STANDARD);
        customer.setLastContactDate(LocalDateTime.now().minusDays(10));
        customer.setNextFollowUpDate(LocalDateTime.now().plusDays(30));
        customer.setCreatedBy("system");
        
        customerRepository.persist(customer);
        LOG.info("Created school customer: " + customer.getCompanyName());
    }

    private void createEventCustomer() {
        Customer customer = new Customer();
        customer.setCustomerNumber("KD-2025-00005");
        customer.setCompanyName("Messe Frankfurt Event GmbH");
        customer.setTradingName("Messe Frankfurt");
        customer.setLegalForm("GmbH");
        customer.setCustomerType(CustomerType.UNTERNEHMEN);
        customer.setIndustry(Industry.EVENT);
        customer.setClassification(Classification.A_KUNDE);
        customer.setHierarchyType(CustomerHierarchyType.STANDALONE);
        customer.setStatus(CustomerStatus.LEAD);
        customer.setLifecycleStage(CustomerLifecycleStage.ACQUISITION);
        customer.setExpectedAnnualVolume(new BigDecimal("300000.00"));
        customer.setActualAnnualVolume(new BigDecimal("0.00"));
        customer.setPaymentTerms(PaymentTerms.NETTO_30);
        customer.setCreditLimit(new BigDecimal("75000.00"));
        customer.setDeliveryCondition(DeliveryCondition.EXPRESS);
        customer.setLastContactDate(LocalDateTime.now().minusDays(3));
        customer.setNextFollowUpDate(LocalDateTime.now().plusDays(5));
        customer.setCreatedBy("system");
        
        customerRepository.persist(customer);
        LOG.info("Created event customer: " + customer.getCompanyName());
    }
}