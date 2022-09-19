val scala212 = "2.12.17"
val scala213 = "2.13.8"
val scala3 = "3.2.0"

ThisBuild / tlBaseVersion := "0.3"

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

lazy val root = tlCrossRootProject.aggregate(core, circe, jsoniterScala, polyline).settings(name := "geo-scala")

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % "3.2.13" % Test,
    "org.scalatestplus" %%% "scalacheck-1-16" % "3.2.13.0" % Test,
    "org.scalacheck" %%% "scalacheck" % "1.16.0" % Test
  ),
  // disable MiMa until we have proper version released on gnieh / all platforms
  // once removed, the tlBaseVersion has to adjusted as well
  mimaPreviousArtifacts := Set.empty
)

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    commonSettings ++ Seq(
      name := "geo-scala-core"
    )
  )

val circeVersion = "0.14.3"
lazy val circe = crossProject(JVMPlatform, JSPlatform, NativePlatform)
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

val jsoniterScalaVersion = "2.17.4"
lazy val jsoniterScala = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("jsoniter-scala"))
  .dependsOn(core)
  .settings(
    commonSettings ++ Seq(
      name := "geo-scala-jsoniter-scala",
      libraryDependencies += "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core" % jsoniterScalaVersion,
      libraryDependencies += "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-macros" % jsoniterScalaVersion % Provided
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
