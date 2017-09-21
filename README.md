# Data Vocalization with CiceroDB

## Contributors

- Immanuel Trummer
- Jiancheng Zhu
- Mark Bryan

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

The project is built and run with [Maven](https://maven.apache.org/).

To run the testing API locally, navigate inside the repository directory and enter the following command

`mvn spring-boot:run`

The application is now running at `localhost:8080` and new tests can be submitted by making a `POST` request
to `localhost:8080/test` with the body of the request specifying the data and configuration
for the test.

### CPLEX

Many of the voice output optimization algorithms use CPLEX to solve linear programs 
that minimize speaking time for conveying data. If you wish to use these algorithms, 
you will need to install the [IBM CPLEX Optimizer](https://www-01.ibm.com/software/commerce/optimization/cplex-optimizer/). 
Once this is installed, add `cplex.jar` in your IDE as a external library. In the installation directory of CPLEX,
you will find the directory `<CPLEX_installation_dir>/bin/<platform>`. Change the JVM argument in `pom.xml` for the location
of your CPLEX installation. This will look like below:

`-Djava.library.path=<CPLEX_installation_dir>/bin/<platform>`

See `pom.xml` for an example. This JVM argument requirement can be explained in further detail [here](http://www-01.ibm.com/support/docview.wss?uid=swg21449776).

## Demo

We have prepared a demo built as a web app to allow users to interact with CiceroDB visually.
The code is located at [https://github.com/mrkbryn/cicero-site](https://github.com/mrkbryn/cicero-site).
