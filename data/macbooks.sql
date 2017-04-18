DROP TABLE IF EXISTS macbooks;
CREATE TABLE macbooks (model VARCHAR(200),
                       inches DOUBLE PRECISION,
                       memory VARCHAR(20),
                       storage VARCHAR(20),
                       dollars INTEGER,
                       gigahertz DOUBLE PRECISION,
                       processor VARCHAR(100),
                       hours_battery_life INTEGER,
                       trackpad VARCHAR(50),
                       pounds DOUBLE PRECISION);
--\copy macbooks FROM 'macbooks.csv' DELIMITER ',' CSV;
\copy macbooks FROM 'macbooks_rounded_prices.csv' DELIMITER ',' CSV;
