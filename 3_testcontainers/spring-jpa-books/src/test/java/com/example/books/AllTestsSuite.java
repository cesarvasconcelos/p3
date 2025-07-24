package com.example.books;

import com.example.books.integration.BookControllerIntegrationTest;
import com.example.books.repository.MagicURLBookRepositoryTest;
import com.example.books.repository.UserRoleAuthenticationDatabaseTests;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import com.example.books.repository.BookRepositoryUsingSpringTestContainersSupportTest;

@Suite
@SuiteDisplayName( "All Tests Suite using JUnit5" )

@SelectClasses( {
    BookControllerIntegrationTest.class,
    BookRepositoryUsingSpringTestContainersSupportTest.class,
    MagicURLBookRepositoryTest.class,
    UserRoleAuthenticationDatabaseTests.class,
})
public class AllTestsSuite {}