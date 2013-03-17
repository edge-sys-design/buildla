import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "buildla"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "pircbot" % "pircbot" % "1.5.0",
    "org.joda" % "joda-convert" % "1.2",
    "joda-time" % "joda-time" % "2.1",
    "commons-net" % "commons-net" % "3.2"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalacOptions += "-feature"
  )

}
