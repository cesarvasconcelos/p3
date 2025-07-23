## How to run the app during development stage:

- `mvn clean package -DskipTests` to **package** and create a Jar file **without** running tests
- `mvn spring-boot:start` **to run** the app using Jar file
- `mvn spring-boot:stop` **to stop** the app using Jar file

### Running Tests
- `mvn clean test -Dspring.profiles.active=test` to run the tests
