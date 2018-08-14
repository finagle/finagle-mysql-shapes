lazy val finagleVersion = "18.7.0"
lazy val circeVersion = "0.9.3"
lazy val catsVersion = "1.1.0"
lazy val shapelessVersion = "2.3.3"
lazy val scalaTestVersion = "3.0.5"
lazy val whiskVersion = "0.9.5"

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
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "com.chuusai" %% "shapeless" % shapelessVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "mysql" % "mysql-connector-java" % "5.1.46",
  "org.scalatest" %%"scalatest" % scalaTestVersion % Test,
  "com.whisk" %% "docker-testkit-scalatest" % "0.10.0-beta4" % Test
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
