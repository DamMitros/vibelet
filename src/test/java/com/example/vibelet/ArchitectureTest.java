package com.example.vibelet;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.vibelet", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {
    @ArchTest
    static final ArchRule controllers_should_not_depend_on_repositories =
            noClasses().that().resideInAPackage("..controller..")
                    .and().haveSimpleNameNotEndingWith("DataExportController")
                    .and().haveSimpleNameNotEndingWith("WebController")
                    .should().dependOnClassesThat().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule services_should_be_in_service_package =
            classes().that().haveSimpleNameEndingWith("Service")
                    .and().resideOutsideOfPackage("..security..")
                    .should().resideInAPackage("..service..");
    @ArchTest
    static final ArchRule repositories_should_be_in_service_package =
            classes().that().haveSimpleNameEndingWith("Repository")
                    .should().resideInAPackage("..repository..");
}