package com.example.books;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration( proxyBeanMethods = false )
public class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  MariaDBContainer mariadbContainer() {
    return new MariaDBContainer( DockerImageName.parse( "mariadb:lts" ) );
  }

}
