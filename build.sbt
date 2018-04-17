name := "Raffle"

version := "1.0"

scalaVersion := "2.12.5"


lazy val http4sVersion = "0.18.8"

// Only necessary for SNAPSHOT releases

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % "0.9.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

scalacOptions ++= Seq(
  "-Xlint",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ypartial-unification"
)
