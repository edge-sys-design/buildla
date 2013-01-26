package com.edgesysdesign.buildbot

import play.api._

object Global extends GlobalSettings {
  val ircbot = new com.edgesysdesign.buildbot.IRCBot()

  override def onStart(app: Application) {
    ircbot.setVerbose(true)
    ircbot.connect("irc.freenode.net", 6667)
    ircbot.joinChannel("#qsolog")
  }

  override def onStop(app: Application) {
    ircbot.disconnect()
  }
}
