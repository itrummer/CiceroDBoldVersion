DROP TABLE IF EXISTS phones;
CREATE TABLE phones (model VARCHAR(200),
                     megapixel_front_camera INTEGER,
                     operating_system VARCHAR(200),
                     grams INTEGER,
                     megapixel_rear_camera INTEGER,
                     gigabytes_of_storage INTEGER,
                     last_attribute DOUBLE PRECISION);
\copy phones FROM 'phones.csv' DELIMITER ',' CSV;
