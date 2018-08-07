lazy val finchVersion = "0.19.0"
lazy val finagleVersion = "18.7.0"
lazy val circeVersion = "0.9.3"
lazy val catsVersion = "1.1.0"
lazy val catbirdVersion = "18.6.0"
lazy val shapelessVersion = "2.3.3"
lazy val slf4jVersion = "1.7.21"
lazy val scalaTestVersion = "3.0.5"
lazy val whiskVersion = "0.9.5"
lazy val configVersion = "1.3.3"

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
  "com.typesafe" % "config" % configVersion,
  "com.github.finagle" %% "finch-core" % finchVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-config" % "0.4.1",
  "com.chuusai" %% "shapeless" % shapelessVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "io.catbird" %% "catbird-util" % catbirdVersion,
  "com.twitter" %% "finagle-mysql" % finagleVersion,
  "mysql" % "mysql-connector-java" % "5.1.46",
  "com.twitter" %% "finagle-stats" % finagleVersion,
  "com.twitter" %% "twitter-server" % finagleVersion,
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
  "com.whisk" %% "docker-testkit-scalatest" % "0.10.0-beta4" % Test,
  "org.scalatest" %%"scalatest" % scalaTestVersion % Test
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    name := "scratchcard-cms",
    libraryDependencies ++= libraries,
    scalafixSettings
  )
