# Data Vocalization with CiceroDB

## Contributors

- Mark Bryan
- Jiancheng Zhu
- Immanuel Trummer

## About
Recent research on data visualization aims at automatically identifying the best way to represent data on visual
interfaces. Supported by a Google faculty Award, the Cornell Database Group is currently studying the 
complementary problem of "data vocalization". The goal here is to optimize the way in which structured data is
represented via audio interfaces. This problem setting is motivated by emerging devices such as Google Home or Amazon
Echo that interact with users primarily over voice based interfaces.

CiceroDB is an experimental database system that will offer different modules 
to translate data into voice output. In the current prototype, we model voice output 
optimization as an NP-hard optimization problem with the goal to reduce speaking time 
for outputting a given data set (subject to user-defined output precision constraints).
The current prototype applies either exhaustive or polynomial time algorithms with approximation 
guarantees to solve the voice output optimization problem and reads out the optimized results. We
currently reduce speaking time by removing redundancies which is appropriate for outputting 
relatively small data sets. In future work, we will explore methods to transmit information that
allow users to get insights about the distributions underlying larger data sets.

## Setup

Note: we are currently working on making this setup process easier and simpler. We will update this section as we do.

The project is built and run with [Maven](https://maven.apache.org/).

You will need to have a PostgreSQL database that CiceroDB can connect to. Install PostgreSQL on your machine and then
create a database. Update the JDBC url, username, and password in `setup.sh`, then run `./setup.sh` to export these
environment variables for Spring to use and connect to your database.

To setup CiceroDB, clone the repository

```bash
git clone https://github.com/itrummer/CiceroDB.git
```

Now, navigate to the CiceroDB directory

```bash
cd ./CiceroDB
```

You can stop here if you don't plan to use the hybrid and linear programming algorithms in `HybridPlanner` and `LinearProgrammingPlanner`.
These algorithms use [IBM's CPLEX Optimizer](https://www-01.ibm.com/software/commerce/optimization/cplex-optimizer/) to solve linear programs and build their voice outputs.
CPLEX uses native libraries instead of pure Java libraries, which prevents us from packaging CiceroDB into one Java project.
In order to use these planners, install the appropriate CPLEX for your machine and then specify the location of 
the native libraries in `pom.xml`. This should be formatted like `-Djava.library.path=<CPLEX_installation_dir>/bin/<platform>`.

You will now be able to run

```bash
mvn test
```

## Ad Hoc Planning

If you wish to run an ad hoc planning test on sample data, you can write a simple script like below.

```Java
// Build a tuple collection from a CSV data source
CSVConnector connector = new CSVConnector();
TupleCollection tuples = connector.buildTupleCollectionFromCSV(
        "restaurants",
        "College Town Bagels,4.5,medium\nMac's Cafe,4.7,high",
        new String[]{"restaurant:STRING", "rating:DOUBLE", "price:STRING"}
);

// Or, you can build a tuple collection from a JDBC data source
SQLConnector connector = new SQLConnector();
TupleCollection tuples = connector.buildTupleCollectionFromQuery(
        "select * from restaurants",
        "restaurants"
);

// Build a configuration to constrain the execution timeout, size, and complexity
Config config = createConfig(2, 2, 2.0);

// Execute the planning algorithm to get a PlanningResult
VoicePlanner planner = new GreedyPlanner();
PlanningResult result = planningManager.buildPlan(planner, tuples, config);

// Print the generated voice output with numbers changed to long-form written text
System.out.println(result.getPlan().toSpeechText(true));

// Print the execution time
System.out.println("Exection Time (in milliseconds): " + result.getExecutionTime());
``` 

## Test API

CiceroDB also has a REST API for submitting tests. To run the testing API locally, navigate inside the repository directory and enter the following command

```bash
mvn spring-boot:run
```

The application will bootstrap the database you setup with test relations, `football`, `macbooks`, `restaurants`, and `yelp`.
The application will be running at `localhost:8080` and new tests can be submitted by making a `POST` request
to `localhost:8080/test` with the body of the request specifying the data and configuration for the test.

See the API for submitting tests [on Swagger](https://swaggerhub.com/apis/CiceroDB/CiceroDB/v1)

## Demo

We have prepared a demo built as a web app to allow users to interact with CiceroDB visually.
The code is located at [https://github.com/mrkbryn/cicero-site](https://github.com/mrkbryn/cicero-site).
