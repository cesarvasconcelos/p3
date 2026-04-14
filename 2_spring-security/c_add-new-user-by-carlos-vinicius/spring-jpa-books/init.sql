-- ------------------------------------------------
-- Create tables
-- ------------------------------------------------
CREATE TABLE tbl_book
(
    book_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_title VARCHAR(255)   NOT NULL,
    book_price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE tbl_user
(
    user_id       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_name     VARCHAR(250) NOT NULL,
    user_password VARCHAR(250) NOT NULL,
    user_fk_role  BIGINT       NOT NULL
);

CREATE TABLE tbl_role
(
    role_id BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role    VARCHAR(250) NOT NULL
);

-- ------------------------------------------------
-- Constraints FK & Unique
-- ------------------------------------------------
ALTER TABLE tbl_user
    ADD CONSTRAINT UNIQ_USERNAME
        UNIQUE (user_name);

ALTER TABLE tbl_role
    ADD CONSTRAINT UNIQ_ROLE
        UNIQUE (role);

ALTER TABLE tbl_user
    ADD CONSTRAINT FK_TO_ROLE_ID
        FOREIGN KEY (user_fk_role) REFERENCES tbl_role (role_id);

-- Each user has a foreign key user_fk_role pointing to tbl_role.role_id.
-- This means that each user must have a role assigned to them.
-- Each role has a unique role name (e.g., ADMIN, USER).
-- One role (e.g., ADMIN) can be referenced by many users.
-- But one user can only have one role in this schema.
-- This is a one-to-many relationship, where one role can be assigned to many users,
-- but each user can only have one role.


insert
tbl_role (role) values ('ROLE_ADMIN');
insert
tbl_role (role) values ('ROLE_USER');

insert
tbl_user (user_name, user_password, user_fk_role)
       values ('admin', '$2a$12$gfTMWrXUwBU.eVPVYbz9C.dPg9kFfRCfL8oYa1TOZg63QCD8nKi1C', 1 );

insert
tbl_user (user_name, user_password, user_fk_role)
       values ('ana', '$2a$12$Q6gFWzwrEUUiaF4kD1M3tOqvuV1N1txnf9hxZtkAk8jLb3U5Gjv.O', 2 );


INSERT INTO tbl_book (book_title, book_price)
VALUES ('The Secrets of the Universe', 19.99),
       ('Adventures in Spring Boot', 25.50),
       ('Mastering Thymeleaf', 29.99),
       ('The Art of MySQL', 35.00),
       ('Bootstrap for Beginners', 15.75),
       ('Deep Dive into JDBC', 27.45),
       ('Spring Security Unleashed', 32.99),
       ('Building Scalable APIs', 40.00),
       ('Java Persistence in Action', 22.50),
       ('Microservices with Spring', 38.95);