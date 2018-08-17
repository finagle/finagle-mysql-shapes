val finagleVersion = "18.7.0"
val mysqlConnectorVersion = "5.1.39"
val circeVersion = "0.9.3"
val catsVersion = "1.1.0"
val shapelessVersion = "2.3.3"
val scalaTestVersion = "3.0.5"
val testcontainersVersion = "1.8.3"
val testcontainersScalaVersion = "0.20.0"

lazy val commonSettings = Seq(
  scalaVersion := "2.12.4",
  scalacOptions ++= Seq(
    "-deprecation",
    "-language:implicitConversions",
    "-encoding", "utf-8",
    "-explaintypes",
    "-feature",
    "-Ywarn-unused:imports",
    "-Ywarn-infer-any",
    "-Ypartial-unification"
  ),
  scalacOptions in (Compile, console) ~= (_.filterNot(_ == "-Ywarn-unused:imports"))
)

lazy val libraries = Seq(
  "com.twitter" %% "finagle-mysql" % finagleVersion,
  "mysql" % "mysql-connector-java" % mysqlConnectorVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.chuusai" %% "shapeless" % shapelessVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.scalatest" %%"scalatest" % scalaTestVersion % Test,
  "org.testcontainers" % "mysql" % testcontainersVersion % Test,
  "com.dimafeng" %% "testcontainers-scala" % testcontainersScalaVersion % Test
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    name := "falcon",
    organization := "com.linecorp",
    version := "0.2-SNAPSHOT",
    publishMavenStyle := true,
    libraryDependencies ++= libraries,
    scalafixSettings
  )

publishTo := {
  val line = "http://repo.linecorp.com/content/repositories/"
  if (isSnapshot.value)
    Some("snapshots" at line + "nightly")
  else
    Some("releases"  at line + "internal")
}
