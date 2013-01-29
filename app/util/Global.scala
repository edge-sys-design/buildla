package com.edgesysdesign.buildbot

import play.api._
import play.api.Play.current

object IRCBot {
  val isEnabled = Play
    .application
    .configuration
    .getBoolean("buildbot.irc_bot")
    .getOrElse(true)

  def sendMessage(message: String) =
    if (isEnabled) {
      Global.ircbot.sendMessage("#qsolog", message)
    }
}

object Global extends GlobalSettings {

  val ircbot = new com.edgesysdesign.buildbot.PIRCBot()

  override def onStart(app: Application) {
    if (IRCBot.isEnabled) {
      ircbot.setVerbose(true)
      ircbot.connect("irc.freenode.net", 6667)
      ircbot.joinChannel("#qsolog")
    }
  }

  override def onStop(app: Application) {
    if (IRCBot.isEnabled) {
      ircbot.disconnect()
    }
  }
}
