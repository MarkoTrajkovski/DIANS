CREATE TABLE StockData (
    id SERIAL PRIMARY KEY,
    issuer_code VARCHAR(10),
    date DATE,
    price_of_last_transaction DECIMAL(10, 2),
    max DECIMAL(10, 2),
    min DECIMAL(10, 2),
    volume INT
);
