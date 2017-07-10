DROP TABLE IF EXISTS football;
CREATE TABLE football (team VARCHAR(150),
                       wins INTEGER,
                       losses INTEGER,
                       win_percentage DOUBLE PRECISION,
                       total_points_for INTEGER,
                       total_points_against INTEGER,
                       net_points_scored INTEGER,
                       touchdowns INTEGER,
                       conference VARCHAR(100));
\copy football FROM 'football.csv' DELIMITER ',' CSV;
