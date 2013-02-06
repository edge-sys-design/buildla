package com.edgesysdesign.buildbot

import com.edgesysdesign.buildbot.util.Build
import com.edgesysdesign.buildbot

import org.jibble.pircbot._

import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global

class PIRCBot extends PircBot {
  setName("esd-buildbot")
  setLogin("esd-buildbot")

  /** Gets called every time a message gets sent to the channel.
    *
    * This is where we handle things like responding to commands.
    *
    * @param channel the channel the message was sent to
    * @param sender the nickname of the person sending the message
    * @param login the ident of the person sending the message
    * @param hostname the hostname/cloak of the person sending the message
    * @param message the message sent to the channel
   */
  override def onMessage(
    channel: String,
    sender: String,
    login: String,
    hostname: String,
    message: String) {
      val messageSplit = message.split(" ", 2)
      messageSplit(0) match {
        case "!build" if (messageSplit.size > 1) => {
          if (channel == "#qsolog") {
            future {
              Build.execute(messageSplit(1), "HEAD")
            }
          } else {
            buildbot.IRCBot.sendMessage("Permission denied.")
          }
        }
        case _ =>
      }
  }


  override def onAction(sender: String,
    login: String,
    hostname: String,
    target: String,
    action: String) =
    onMessage(target, sender, login, hostname, action)

  /** Reconnect when we get disconnected from the network. */
  override def onDisconnect() = new PIRCBot()
}
