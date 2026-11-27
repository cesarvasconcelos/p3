package com.example.books;

import com.example.books.integration.BookControllerIntegrationTest;
import com.example.books.repository.MagicURLBookRepositoryTest;
import com.example.books.repository.UserRoleAuthenticationDatabaseTests;
import com.example.books.service.BookServiceTest;
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
    BookServiceTest.class
})
public class AllTestsSuite {}
 /* Acima, com exceção de BookServiceTest, que não usa Testcontainers (apenas Mockito),
 cada classe de teste usa um único container MariaDB do Testcontainers compartilhado
 entre seus métodos @Test. Não é um container por @Test método; */