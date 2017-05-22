DROP TABLE IF EXISTS yelp;
CREATE TABLE yelp (restaurant VARCHAR(200),
                     rating DOUBLE PRECISION,
                     price VARCHAR(200),
                     reviews INTEGER,
                     location VARCHAR(200),
                     cuisine VARCHAR(200));
\copy yelp FROM 'yelp.csv' DELIMITER ',' CSV;
