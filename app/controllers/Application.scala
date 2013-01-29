package controllers

import com.edgesysdesign.buildbot.util.Build

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action { implicit request =>
    val allowed = Play
      .application
      .configuration
      .getStringList("buildbot.allowed_ips")
      .get

    val payload = request.body.asFormUrlEncoded.get("payload")(0)

    if (allowed.contains(request.remoteAddress)) {
      val json = Json.parse(payload)

      val repo = (json \ "repository" \ "url")
        .as[String]
        .split("\\/", 4)
        .last

      val afterCommit = (json \ "after").as[String].take(8)

      val build = Build.execute(repo, afterCommit)

      build match {
        case Left(error) => BadRequest(error)
        case Right(result) => Ok("Done.")
      }
    } else {
      Unauthorized("denied")
    }
  }
}
