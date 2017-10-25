import sbt.Keys._

val appName = "scalaio-microservices"
val appVersion = "1.0"
val sVersion = "2.12.4"
val akkaV = "2.5.6"
val akkaHttpV = "10.0.10"

      val appDependencies = Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.typesafe.akka" %% "akka-cluster" % akkaV,
        "com.typesafe.akka" %% "akka-cluster-sharding" % akkaV,
        "com.typesafe.akka" %% "akka-persistence" % akkaV,
        "com.typesafe.akka" %% "akka-stream-kafka" % "0.17",
        "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.30",
        "com.typesafe.akka" %% "akka-http" % akkaHttpV,
        "com.typesafe.akka" % "akka-cluster-metrics_2.12" % akkaV
      )

      val modelDependencies = Seq(
        "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
        "com.github.melrief" %% "purecsv" % "0.1.1")

lazy val model = (project in file("model"))
  .settings(name := appName + "-model")
  .settings(version := appVersion)
  .settings(scalaVersion := sVersion)
  .settings(libraryDependencies ++= modelDependencies)

lazy val app = (project in file("app"))
  .settings(name := appName + "-app")
  .settings(version := appVersion)
  .settings(scalaVersion := sVersion)
  .settings(libraryDependencies ++= appDependencies)
  .dependsOn(model)

lazy val root = (project in file("."))
  .settings(name := appName)
  .settings(version := appVersion)
  .settings(scalaVersion := sVersion)
  .aggregate(app, model)
  .dependsOn(app, model)

