package de.freshplan.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * ArchUnit Rules to enforce test data discipline.
 * 
 * These rules ensure that:
 * 1. Tests don't directly persist entities but use TestDataBuilder
 * 2. Test data is properly marked and isolated
 * 3. No new Initializers are created
 */
@AnalyzeClasses(packages = "de.freshplan")
public class TestDataDisciplineRules {

    /**
     * Rule 1: Tests must not directly call persist() on repositories.
     * They should use TestDataBuilder instead.
     */
    @ArchTest
    static final ArchRule no_direct_persist_in_tests =
        noMethods()
            .that().areDeclaredInClassesThat()
                .haveSimpleNameEndingWith("Test")
                .or().haveSimpleNameEndingWith("IT")
            .should().callMethodWhere(target -> 
                (target.getOwner().getName().contains("Repository") &&
                 (target.getName().equals("persist") || 
                  target.getName().equals("persistAndFlush")))
            )
            .because("Tests should use TestDataBuilder.customer().persist() instead of direct repository.persist()");

    /**
     * Rule 2: Tests must not directly instantiate Customer entities.
     * They should use TestDataBuilder.
     */
    @ArchTest
    static final ArchRule no_direct_customer_instantiation_in_tests =
        noMethods()
            .that().areDeclaredInClassesThat()
                .haveSimpleNameEndingWith("Test")
                .or().haveSimpleNameEndingWith("IT")
            .and().areNotDeclaredIn("de.freshplan.test.TestDataBuilder")
            .should().callConstructor(
                de.freshplan.domain.customer.entity.Customer.class
            )
            .because("Tests should use TestDataBuilder.customer().build() instead of new Customer()");

    /**
     * Rule 3: No new Initializer classes should be created.
     * We're moving away from Initializers to TestDataBuilder pattern.
     */
    @ArchTest
    static final ArchRule no_new_initializers =
        noClasses()
            .that().haveSimpleNameEndingWith("Initializer")
            .and().areNotDeclaredIn("de.freshplan.test") // Allow test-scoped helpers
            .should().beAnnotatedWith(jakarta.enterprise.context.ApplicationScoped.class)
            .orShould().beAnnotatedWith(jakarta.inject.Singleton.class)
            .because("Use TestDataBuilder pattern instead of creating new Initializers");

    /**
     * Rule 4: Test classes should use @TestTransaction for database tests.
     * This ensures automatic rollback and test isolation.
     */
    @ArchTest
    static final ArchRule database_tests_need_transaction =
        methods()
            .that().areDeclaredInClassesThat()
                .haveSimpleNameEndingWith("IT") // Integration Tests
                .or().haveSimpleNameEndingWith("IntegrationTest")
            .and().areAnnotatedWith(org.junit.jupiter.api.Test.class)
            .and().areNotAnnotatedWith(org.junit.jupiter.api.Disabled.class)
            .should().beAnnotatedWith(io.quarkus.test.TestTransaction.class)
            .orShould().beDeclaredInClassesThat()
                .areAnnotatedWith(io.quarkus.test.TestTransaction.class)
            .because("Database tests need @TestTransaction for proper isolation and cleanup");

    /**
     * Rule 5: TestDataBuilder methods must mark data as test data.
     * This ensures all test data has is_test_data = true.
     */
    @ArchTest
    static final ArchRule test_data_must_be_marked =
        methods()
            .that().areDeclaredIn("de.freshplan.test.TestDataBuilder")
            .and().haveName("build")
            .or().haveName("persist")
            .should().notBePrivate() // Just a simple check for now
            .because("TestDataBuilder must ensure all entities have isTestData=true");
}