DROP TABLE IF EXISTS restaurants;
CREATE TABLE restaurants (restaurant VARCHAR(200),
                          rating DOUBLE PRECISION,
                          price VARCHAR(30),
                          cuisine VARCHAR(100));
\copy restaurants FROM 'restaurants.csv' DELIMITER ',' CSV;
