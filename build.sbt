name := """cicero-db"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

libraryDependencies += javaJdbc
libraryDependencies += cache
libraryDependencies += javaWs
libraryDependencies += "com.ibm.watson.developer_cloud" % "java-sdk" % "3.6.0"
libraryDependencies += "org.postgresql" % "postgresql" % "42.0.0.jre7"

herokuAppName in Compile := "cicero-db"
