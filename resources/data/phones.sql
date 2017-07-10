DROP TABLE IF EXISTS phones;
CREATE TABLE phones (model VARCHAR(200),
                     core_processors INTEGER,
                     operating_system VARCHAR(200),
                     grams DOUBLE PRECISION,
                     megapixels DOUBLE PRECISION,
                     gigabytes_of_storage INTEGER,
                     gigabytes_of_ram DOUBLE PRECISION);
\copy phones FROM 'phones.csv' DELIMITER ',' CSV;
