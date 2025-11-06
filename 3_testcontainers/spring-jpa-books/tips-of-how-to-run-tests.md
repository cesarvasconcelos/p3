## How to run the app during development stage:

- `mvn clean package -DskipTests` to **package** and create a Jar file **without** running tests
- `mvn spring-boot:start` **to run** the app using Jar file
- `mvn spring-boot:stop` **to stop** the app using Jar file

### Running Tests
- `mvn clean test -Dspring.profiles.active=test` to run the tests

### Running a single test class (only)
Use Maven Surefire's `-Dtest` with the `test` profile activated:

- Run only `BookControllerIntegrationTest`:
  - `mvn -Dspring.profiles.active=test -Dtest=com.example.books.integration.BookControllerIntegrationTest test`

- Run only `BookRepositoryUsingSpringTestContainersSupportTest`:
  - `mvn -Dspring.profiles.active=test -Dtest=com.example.books.repository.BookRepositoryUsingSpringTestContainersSupportTest test`

Tip: you can also run a single test method by appending `#methodName`:
- `mvn -Dspring.profiles.active=test -Dtest=com.example.books.integration.BookControllerIntegrationTest#givenAuthenticatedUser_whenPostBook_thenItIsSavedAndRedirects test`
