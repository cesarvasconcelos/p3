-- ------------------------------------------------
-- Create tables
-- ------------------------------------------------
CREATE TABLE tbl_book
(
    book_id         BIGINT              AUTO_INCREMENT PRIMARY KEY,
    book_title      VARCHAR(255)        NOT NULL,
    book_price      DECIMAL(10, 2)      NOT NULL
);

CREATE TABLE tbl_user
(
    user_id         BIGINT                NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_name       VARCHAR(250)          NOT NULL,
    user_password   VARCHAR(250)          NOT NULL,
    user_fk_role    BIGINT                NOT NULL
);

CREATE TABLE tbl_role
(
    role_id         BIGINT                NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role            VARCHAR(250)          NOT NULL
);

-- ------------------------------------------------
-- Constraints FK & Unique
-- ------------------------------------------------
ALTER TABLE tbl_role
    ADD CONSTRAINT UNIQ_ROLE
        UNIQUE (role);

ALTER TABLE tbl_user
    ADD CONSTRAINT FK_TO_ROLE_ID
        FOREIGN KEY (user_fk_role) REFERENCES tbl_role (role_id);

-- ------------------------------------------------
-- Important Note: With @WithMockUser annotation, do I really need to have the user/role tables?
-- ------------------------------------------------
-- Tests that use @WithMockUser simulate an authenticated user in memory and
-- do NOT require the above tbl_user/tbl_role tables to pass.
-- *However*, UserRoleAuthenticationDatabaseTests persists and reads User/Role
-- entities via JPA repositories and therefore DEPENDS on these tables and
-- the foreign key user_fk_role.
-- So keep this DDL when running the full suite (AllTestsSuite), or remove/skip
-- UserRoleAuthenticationDatabaseTests if you choose not to create user/role tables.

-- insert tbl_role (role) values ('admin');
-- insert tbl_role (role) values ('student');
-- insert tbl_user (user_name, user_password, user_fk_role)
--        values ('admin', '$2a$12$gfTMWrXUwBU.eVPVYbz9C.dPg9kFfRCfL8oYa1TOZg63QCD8nKi1C', 1 );
-- insert tbl_user (user_name, user_password, user_fk_role)
--        values ('ana', '$2a$12$Q6gFWzwrEUUiaF4kD1M3tOqvuV1N1txnf9hxZtkAk8jLb3U5Gjv.O', 2 );