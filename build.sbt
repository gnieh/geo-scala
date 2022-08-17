val scala212 = "2.12.16"
val scala213 = "2.13.8"
val scala3 = "3.1.3"

ThisBuild / tlBaseVersion := "0.2"

ThisBuild / organization := "org.gnieh"
ThisBuild / organizationName := "GHM Mobile Development GmbH"
ThisBuild / startYear := Some(2019)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("satabin", "Lucas Satabin"),
  tlGitHubDev("ybasket", "Yannick Heiber")
)
ThisBuild / tlSonatypeUseLegacyHost := true

ThisBuild / crossScalaVersions := Seq(scala212, scala213, scala3)
ThisBuild / scalaVersion := scala213 // the default Scala

lazy val root = tlCrossRootProject.aggregate(core, circe, polyline).settings(name := "geo-scala")

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % "3.2.12" % Test,
    "org.scalatestplus" %%% "scalacheck-1-16" % "3.2.13.0" % Test,
    "org.scalacheck" %%% "scalacheck" % "1.16.0" % Test
  )
)

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    commonSettings ++ Seq(
      name := "geo-scala-core"
    )
  )

val circeVersion = "0.14.2"
lazy val circe = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("circe"))
  .dependsOn(core)
  .settings(
    commonSettings ++ Seq(
      name := "geo-scala-circe",
      libraryDependencies += "io.circe" %%% "circe-core" % circeVersion,
      libraryDependencies += "io.circe" %%% "circe-parser" % circeVersion % Test
    )
  )

lazy val polyline = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("polyline"))
  .dependsOn(core)
  .settings(
    commonSettings ++ Seq(
      name := "geo-scala-polyline"
    )
  )
