logLevel := Level.Warn

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("de.heikoseeberger" %% "sbt-groll" % "6.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.0")