-- ------------------------------------------------
-- Create tables
-- ------------------------------------------------
CREATE TABLE book
(
    id         BIGINT              AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(255)        NOT NULL,
    price      DECIMAL(10, 2)      NOT NULL
);
