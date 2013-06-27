import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "usernamer-server"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    // XXX jdbc,
    // XXX anorm
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // resolvers ++= Seq(
    //   "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
    //   "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/"
    // ),
    // libraryDependencies ++= Seq(
    //   "play-autosource"   %% "reactivemongo"       % "0.1-SNAPSHOT"
    // )
    // FIXME? What's the difference between putting this here vs in appDependencies? Where does this even come from?~run
    libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.9"
  )

}