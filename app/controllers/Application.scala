package controllers

import com.edgesysdesign.buildbot.Global
import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.libs.json.Json

import org.joda.time.{DateTime, Period}
import org.joda.time.format.PeriodFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.sys.process._

import java.io.File

object Application extends Controller {

  def index = Action { implicit request =>
    val allowed = Play
      .application
      .configuration
      .getStringList("buildbot.allowed_ips")
      .get

    val buildDirectory = Play
      .application
      .configuration
      .getString("buildbot.repos_directory")
      .get

    val payload = request.body.asFormUrlEncoded.get("payload")(0)

    if (allowed.contains(request.remoteAddress)) {
      val json = Json.parse(payload)

      val repo = (json \ "repository" \ "url")
        .as[String]
        .split("\\/", 4)
        .last

      val afterCommit = (json \ "after").as[String].take(8)

      val buildDir = s"${buildDirectory}/${repo}"
      val buildScript = s"${buildDir}/build.sh"
      if (!new File(buildScript).exists()) {
        BadRequest(s"No buildscript found for ${repo}.")
      } else {
        Global.ircbot.sendMessage(
          "#qsolog",
          s"Starting build of ${2.toChar}${repo}${2.toChar} at " +
          s"${2.toChar}${afterCommit}${2.toChar}.")

        val startTime = new DateTime
        val exitCode = Seq("bash", buildScript) !
        val endTime = new DateTime
        val timeLength = PeriodFormat.getDefault.print(
          new Period(startTime, endTime))

        val status = exitCode match {
          case 0 => s"${3.toChar}030${3.toChar}"
          case code => s"${3.toChar}05${code}${3.toChar}"
        }

        Global.ircbot.sendMessage(
          "#qsolog",
          s"Build of ${2.toChar}${repo}${2.toChar} finished with exit code " +
          s"${2.toChar}${status}${2.toChar} after " +
          s"${2.toChar}${timeLength}${2.toChar}.")

        // TODO: This isn't Async'd because we can't handle locking good enough.
        // If we want to get that advanced, we can later on look into using a
        // queue server daemon to handle this. For now, queue the HTTP requests.
        Ok("Done.")
      }
    } else {
      Unauthorized("denied")
    }
  }
}
