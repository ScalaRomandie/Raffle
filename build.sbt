name := "Raffle"

version := "1.0"

scalaVersion := "2.11.8"


lazy val http4sVersion = "0.14.6"

// Only necessary for SNAPSHOT releases

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" %  http4sVersion,
  "io.circe" %% "circe-generic" % "0.4.1",
  "ch.qos.logback" % "logback-classic" % "1.1.7"
)