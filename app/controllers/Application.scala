package controllers

import com.edgesysdesign.buildbot.util.Build

import org.apache.commons.net.util.SubnetUtils

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action { implicit request =>
    val githubRequest = WS.url("https://api.github.com/meta").get()

    Async {
      githubRequest.map { response =>
        val ips = "127.0.0.1/32" :: (response.json \ "hooks").as[List[String]]
        if (ips.exists(subnet => new SubnetUtils(subnet).getInfo.isInRange(request.remoteAddress))) {
          val payload = request.body.asFormUrlEncoded.get("payload")(0)
          val json = Json.parse(payload)
          val repo = (json \ "repository" \ "url").as[String].split("\\/", 4).last
          val afterCommit = (json \ "after").as[String].take(8)
          val build = Build.execute(repo, afterCommit)

          build match {
            case Left(error) => BadRequest(error)
            case Right(result) => Ok("Done.")
          }
        } else {
          Unauthorized("Access Denied")
        }
      }
    }
  }
}
