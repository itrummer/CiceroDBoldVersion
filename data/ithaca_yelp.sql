CREATE TABLE ithaca_yelp_1 (name VARCHAR(200), city VARCHAR(100), state VARCHAR(20), review_count INTEGER, category VARCHAR(250), price INTEGER);
\copy ithaca_yelp_1 FROM 'ithaca_yelp_1.csv' DELIMITER ',' CSV;
