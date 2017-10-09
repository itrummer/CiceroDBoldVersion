-- This script initialize the database schema

DROP TABLE IF EXISTS football;
CREATE TABLE football (
  team                 VARCHAR(150),
  wins                 INTEGER,
  losses               INTEGER,
  win_percentage       DOUBLE PRECISION,
  total_points_for     INTEGER,
  total_points_against INTEGER,
  net_points_scored    INTEGER,
  touchdowns           INTEGER,
  conference           VARCHAR(100)
);

DROP TABLE IF EXISTS restaurants;
CREATE TABLE restaurants (
  restaurant VARCHAR(200),
  rating     DOUBLE PRECISION,
  price      VARCHAR(30),
  cuisine    VARCHAR(100)
);

DROP TABLE IF EXISTS macbooks;
CREATE TABLE macbooks (
  model                VARCHAR(200) NOT NULL,
  inch_display         DOUBLE PRECISION,
  gigabytes_of_memory  INTEGER,
  gigabytes_of_storage INTEGER,
  dollars              INTEGER,
  gigahertz            DOUBLE PRECISION,
  processor            VARCHAR(100),
  hours_battery_life   INTEGER,
  trackpad             VARCHAR(50),
  pounds               DOUBLE PRECISION,
  PRIMARY KEY (model)
);

DROP TABLE IF EXISTS phones;
CREATE TABLE phones (
  model VARCHAR(200),
  core_processors INTEGER,
  operating_system VARCHAR(200),
  grams DOUBLE PRECISION,
  megapixels DOUBLE PRECISION,
  gigabytes_of_storage INTEGER,
  gigabytes_of_ram DOUBLE PRECISION
);

DROP TABLE IF EXISTS yelp;
CREATE TABLE yelp (
  restaurant VARCHAR(200),
  rating DOUBLE PRECISION,
  price VARCHAR(200),
  reviews INTEGER,
  location VARCHAR(200),
  cuisine VARCHAR(200)
);
