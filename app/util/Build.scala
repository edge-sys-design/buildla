package com.edgesysdesign.buildbot.util

import com.edgesysdesign.buildbot.{Global, IRCBot}

import org.joda.time.{DateTime, Period}
import org.joda.time.format.PeriodFormat

import play.api._
import play.api.Play.current

import scala.sys.process._

import java.io.File

object Build {
  case class BuildResult(time: String, exitCode: Int)

  def execute(repo: String, commit: String): Either[String, BuildResult] = {
    val buildDirectory = Play
      .application
      .configuration
      .getString("buildbot.repos_directory")
      .get

    val buildDir = s"${buildDirectory}/${repo}"
    val buildScript = s"${buildDir}/build.sh"

    if (!new File(buildScript).exists()) {
      Left(s"No buildscript found for ${repo}.")
    } else {
      IRCBot.sendMessage(
        s"Starting build of ${2.toChar}${repo}${2.toChar} at " +
        s"${2.toChar}${commit}${2.toChar}.")

      val startTime = new DateTime
      val exitCode = Seq("bash", buildScript) !
      val endTime = new DateTime
      val timeLength = PeriodFormat.getDefault.print(
        new Period(startTime, endTime))

      val status = exitCode match {
        case 0 => s"${3.toChar}030${3.toChar}"
        case code => s"${3.toChar}05${code}${3.toChar}"
      }

      IRCBot.sendMessage(
        s"Build of ${2.toChar}${repo}${2.toChar} finished with exit code " +
        s"${2.toChar}${status}${2.toChar} after " +
        s"${2.toChar}${timeLength}${2.toChar}.")

      Right(BuildResult(timeLength, exitCode))
    }
  }
}
