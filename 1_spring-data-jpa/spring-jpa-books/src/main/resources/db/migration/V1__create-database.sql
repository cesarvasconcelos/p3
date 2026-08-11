-- ------------------------------------------------
-- Create tables
-- ------------------------------------------------
CREATE TABLE tbl_book
(
    book_id         BIGINT              AUTO_INCREMENT PRIMARY KEY,
    book_title      VARCHAR(255)        NOT NULL,
    -- DECIMAL(10,2): 10 = total de dígitos (precisão), 2 = dígitos após a vírgula (escala)
    -- => 8 dígitos antes da vírgula; valor máximo: 99999999.99
    book_price      DECIMAL(10, 2)      NOT NULL
);