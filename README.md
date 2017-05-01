# Audiolization (CiceroDB: Optimizing Voice Output of Relational Data)

Recent research on data visualization aims at automatically identifying the best way to represent data on visual
interfaces. Supported by a Google faculty Award, the Cornell Database Group is currently studying the 
complementary problem of "data audiolization". The goal here is to optimize the way in which structured data is
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

## Environment Setup

The project uses the [sbt](http://www.scala-sbt.org/download.html) build tool, so ensure this is installed before continuing.

Clone the repository and then import the project into IntelliJ as an sbt project. You will now be able
to install the required dependencies using sbt, or you can allow IntelliJ to auto-import dependencies.

To run the project, navigate to the `audiolization` directory and enter the following command in the command line 

`sbt run`

Open a web browser and navigate to `localhost:9000` to view the app.

### CPLEX Setup

If you wish to use the `IntegerProgrammingPlanner`, you will need to install the
[IBM CPLEX Optimizer](https://www-01.ibm.com/software/commerce/optimization/cplex-optimizer/). 
CiceroDB uses CPLEX to solve the integer programming model of speech plan optimization. Once this is
installed, add `cplex.jar` in your IDE as a external library. In the installation directory of CPLEX,
you will find the directory `<CPLEX_installation_dir>/bin/<platform>`. You will need to add the following
Java VM argument

`-Djava.library.path=<CPLEX_installation_dir>/bin/<platform>`

For example, this could be something like

`/Users/mabryan/Applications/IBM/ILOG/CPLEX_Studio_Community127/cplex/bin/x86-64_osx`

This VM argument requirement can be explained in further detail [here](http://www-01.ibm.com/support/docview.wss?uid=swg21449776).

## Contributors

- Immanuel Trummer
- Jiancheng Zhu
- Mark Bryan
