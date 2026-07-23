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

-- Each user has a foreign key user_fk_role pointing to tbl_role.role_id.
-- This means that each user must have a role assigned to them.
-- Each role has a unique role name (e.g., ADMIN, USER).
-- One role (e.g., ADMIN) can be referenced by many users.
-- But one user can only have one role in this schema.
-- This is a one-to-many relationship, where one role can be assigned to many users,
-- but each user can only have one role.