# IFPB - Programação III

Instituto Federal de Educação, Ciência e Tecnologia da Paraíba - IFPB - *Campus*
CG  / PB

**Disciplina**: Programação III.

Neste repositório, encontram-se exemplos de código utilizados em sala de aula.
Os códigos não devem ser usados como único material de referência para estudo.
Há trechos de códigos incompletos, com erros e/ou outros problemas de
implementação, os quais devem ser analisados pelo aluno como exercício de
programação.

## Objetivos de aprendizado

- Como projetar e implementar aplicações Web coorporativas usando framework [Spring](https://spring.io/projects/spring-framework)
- Implementar o front-end e back-end de uma aplicação Web típica
- Operações CRUD, submissão de formulários HTML, validação e integração com banco de dados
- Spring ecossistema: [Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html), [Spring Integration](https://spring.io/projects/spring-integration), [Spring Testing](https://docs.spring.io/spring-framework/reference/testing.html), [Spring Boot](https://spring.io/projects/spring-boot), [Spring Data JPA](https://spring.io/projects/spring-data-jpa), [Spring Security](https://spring.io/projects/spring-security) e [Spring AOP](https://docs.spring.io/spring-framework/reference/core/aop.html)
- No Spring Security, como realizar autenticação com login, gerência de sessões e autorização baseada em privilégios
- Discutir boas práticas no projeto de aplicações orientadas a microserviços
- Construir REST APIs e Aplicações Web com [Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html), [Thymeleaf](https://www.thymeleaf.org/), [Bootstrap CSS](https://getbootstrap.com/)
- Persistência em banco de dados usando [Spring Data JDBC](https://spring.io/projects/spring-data-jdbc)/[JPA](https://spring.io/projects/spring-data-jpa), [MySQL](https://dev.mysql.com/doc/)/[MariaDB](https://mariadb.org/), [Flyway](https://flywaydb.org/)
- Como utilizar ORM Frameworks (e.g., [Hibernate](https://hibernate.org/orm/))
- Setup do desenvolvimento local com [Docker](https://docs.docker.com/), [Docker Compose](https://docs.docker.com/compose/), [Testcontainers](https://testcontainers.com/)
- Usar o [JUnit](https://junit.org/), [AssertJ](https://assertj.github.io/doc/), [Mockito](https://site.mockito.org/) para testes

## Material de Referência

Os principais materiais de estudo usados na disciplina são os seguintes livros: [Spring Start
Here](https://www.manning.com/books/spring-start-here) e [Use a Cabeça! HTML e CSS](https://www.amazon.com.br/Use-Cabe%C3%A7a-HTML-Eric-Freeman/dp/8576088622).
Outras fontes de leitura serão indicadas pelo professor.

As documentações oficiais:

- [Spring Website](https://spring.io/)
- [Spring Projects](https://spring.io/projects)
- [Spring Framework Javadoc API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/)
- [HTML Reference](https://developer.mozilla.org/en-US/docs/Web/HTML/Reference)
- [CSS Reference](https://developer.mozilla.org/en-US/docs/Web/CSS/Reference)
- [Thymeleaf](https://www.thymeleaf.org/)

Outros links:

- [Spring initializr](https://start.spring.io/) para gerar projetos. Alternativamente, pode-se usar
  o comando `spring init` via linha de comando (instalável via [SDKMAN](https://sdkman.io/sdks/springboot/)) com parâmetros, por exemplo:
  ```bash
  spring init \
  --name=my-web-app \
  --groupId=com.example \
  --artifactId=my-web-app \
  --package-name=com.example.mywebapp \
  --dependencies=web,security,data-jpa,mariadb,docker-compose,testcontainers,thymeleaf,validation,flyway \
  --java-version=21 \
  --packaging=jar \
  --version=0.0.1-SNAPSHOT \
  --boot-version=3.5.7 \
  --build=maven \
  my-web-app
  ```

## Suporte ferramental básico

- Um navegador (Firefox, Brave, Google Chrome, Opera, Safari, etc.)
- Uma distribuição OpenJDK (e.g., [Amazon Correto](https://aws.amazon.com/corretto/)) - pode ser instalada via [SDKMAN](https://sdkman.io/)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [Apache Maven](https://maven.apache.org/)
- [Docker Desktop](https://docs.docker.com/get-started/get-docker/)
- Um editor de texto (e.g., [Visual Studio Code](https://code.visualstudio.com/) ou [TRAE](https://trae.ai/))
- Spring Boot CLI (instalável via [SDKMAN](https://sdkman.io/)) para usar o comando `spring init`
- Algumas [extensões](https://marketplace.visualstudio.com/vscode) do Visual Studio Code, que serão sugeridas pelo professor.

## Este repositório

Pode-se obter apenas os [códigos](https://github.com/cesarvasconcelos/daweb1/archive/master.zip) ou, alternativamente, utilizar o [Git](https://git-scm.com/) para clonar todo o repositório funcional e seu histórico de versões:

```
$ git clone https://github.com/cesarvasconcelos/p3.git
```

## Autor

* **Prof. Dr. César Vasconcelos (cesarocha@ifpb.edu.br)** - [GitHub Page](https://github.com/cesarvasconcelos)


