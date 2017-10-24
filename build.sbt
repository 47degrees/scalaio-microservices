import com.typesafe.sbt.packager.SettingsHelper
import sbt.Keys._

val appName = "scalaio-microservices"
val appVersion = "1.0"
scalaVersion := "2.11.8"
//scalaVersion := "2.12.4"

//val akkaV = "2.5.6"
val akkaV = "2.4.18"

val appDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-cluster" % akkaV,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaV,
//  "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaV,
  "com.typesafe.akka" %% "akka-persistence" % akkaV,
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.17",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "org.iq80.leveldb" % "leveldb" % "0.9",
)

lazy val model = (project in file("model"))
  .settings(name := appName + "-model")
  .settings(version := appVersion)
  .enablePlugins(JavaServerAppPackaging)

lazy val app = (project in file("app"))
  .settings(name := appName + "-app")
  .settings(version := appVersion)
  .settings(libraryDependencies ++= appDependencies)
  .enablePlugins(JavaServerAppPackaging)
  .dependsOn(model)

lazy val root = (project in file("."))
  .settings(name := appName)
  .settings(version := appVersion)
  .aggregate(app, model)

