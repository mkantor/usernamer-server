import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "usernamer-server"
  val appVersion = "0.1-SNAPSHOT"

  val appDependencies = Seq(
    "org.reactivemongo" %% "play2-reactivemongo" % "0.9",
    "info.schleichardt" %% "play-embed-mongo" % "0.2" % "test"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint")
  )

}