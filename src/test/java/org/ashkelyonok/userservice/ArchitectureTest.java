package org.ashkelyonok.userservice;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "org.ashkelyonok.userservice", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule controllers_should_only_use_services = classes()
            .that().resideInAPackage("..controller..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..service..",
                    "..model..",
                    "..controller..",
                    "java..",
                    "org.springframework..",
                    "jakarta..",
                    "org.slf4j..",
                    "io.swagger..",
                    "org.springdoc..",
                    "lombok.."
            );

    @ArchTest
    static final ArchRule services_should_be_annotated = classes()
            .that().resideInAPackage("..service.impl..")
            .should().beAnnotatedWith("org.springframework.stereotype.Service");

    @ArchTest
    static final ArchRule no_cycles = com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
            .matching("org.ashkelyonok.userservice.(*)..")
            .should().beFreeOfCycles();

}
