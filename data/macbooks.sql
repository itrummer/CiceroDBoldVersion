DROP TABLE IF EXISTS macbooks;
CREATE TABLE macbooks (model VARCHAR(200) NOT NULL,
                       inch_display DOUBLE PRECISION,
                       gigabytes_of_memory INTEGER,
                       gigabytes_of_storage INTEGER,
                       dollars INTEGER,
                       gigahertz DOUBLE PRECISION,
                       processor VARCHAR(100),
                       hours_battery_life INTEGER,
                       trackpad VARCHAR(50),
                       pounds DOUBLE PRECISION,
                       PRIMARY KEY (model));
--\copy macbooks FROM 'macbooks.csv' DELIMITER ',' CSV;
\copy macbooks FROM 'macbooks.csv' DELIMITER ',' CSV;
