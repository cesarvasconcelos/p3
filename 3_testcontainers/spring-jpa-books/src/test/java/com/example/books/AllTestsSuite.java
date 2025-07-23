package com.example.books;

import com.example.books.repository.UserRoleAuthenticationDatabaseTests;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import com.example.books.repository.BookRepositoryUsingSpringTestContainersSupportTest;

@Suite
@SuiteDisplayName( "A demo Test Suite using JUnit5" )

@SelectClasses( {
    BookRepositoryUsingSpringTestContainersSupportTest.class,
    UserRoleAuthenticationDatabaseTests.class,
})
public class AllTestsSuite {}