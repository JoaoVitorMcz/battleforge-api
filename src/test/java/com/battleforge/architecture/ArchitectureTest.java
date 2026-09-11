package com.battleforge.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

/**
 * Turns the architecture described in the README into rules the build enforces.
 *
 * <p>The battle engine is the asset worth protecting: it must stay plain Java, so it can be
 * unit tested with no container and reasoned about without framework knowledge. Catalog
 * entities under domain.model are the deliberate exception, since they are the cached
 * PokeAPI data and are mapped with JPA.
 */
@AnalyzeClasses(
        packages = "com.battleforge",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String ENGINE_PACKAGES =
            "com.battleforge.domain.(battle|strategy|item|ai|state)..";

    @ArchTest
    static final ArchRule domainDoesNotDependOnSpring = ArchRuleDefinition.noClasses()
            .that().resideInAPackage("com.battleforge.domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
            .because("the domain must be usable and testable without a Spring context")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domainDoesNotDependOnTheIntegrationLayer = ArchRuleDefinition.noClasses()
            .that().resideInAPackage("com.battleforge.domain..")
            .should().dependOnClassesThat().resideInAPackage("com.battleforge.integration..")
            .because("PokeAPI payloads are translated by mappers; the domain never sees them")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule battleEngineDoesNotDependOnPersistence = ArchRuleDefinition.noClasses()
            .that().resideInAPackage(ENGINE_PACKAGES)
            .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "com.battleforge.repository..")
            .because("turn resolution, damage and item hooks are pure logic, not persisted state")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controllersDoNotUseRepositories = ArchRuleDefinition.noClasses()
            .that().resideInAPackage("com.battleforge.controller..")
            .should().dependOnClassesThat().resideInAPackage("com.battleforge.repository..")
            .because("controllers delegate to services and never reach the database directly")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controllersDoNotExposeEntities = ArchRuleDefinition.noClasses()
            .that().resideInAPackage("com.battleforge.controller..")
            .should().dependOnClassesThat().areAnnotatedWith(jakarta.persistence.Entity.class)
            .because("the API speaks DTO records; JPA entities stay behind the service layer")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule packagesAreFreeOfCycles = SlicesRuleDefinition.slices()
            .matching("com.battleforge.(*)..")
            .should().beFreeOfCycles();
}
