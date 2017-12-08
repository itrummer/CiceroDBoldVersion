#!/bin/bash

# Run this script before executing mvn spring-boot:run in order to
# ensure that Spring can connect to the correct database

# Update the following to reflect the preferred database
# you wish to use for CiceroDB. It is recommended to create
# a database called "cicero" for CiceroDB to work with, and to
# include these environment variables in your bash_profile.
export JDBC_DATABASE_URL="jdbc:postgresql://localhost:5432/cicero"
export JDBC_DATABASE_USERNAME="postgres"
export JDBC_DATABASE_PASSWORD=""
