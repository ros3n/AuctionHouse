package main

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{OneForOneStrategy, Props, ActorRef, Actor}
import akka.event.LoggingReceive

object Notifier {
  sealed trait NotifierMessage

  case class Notify(auction: ActorRef, buyer: ActorRef, price: BigInt) extends NotifierMessage
}

class Notifier extends Actor {

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: Exception => Stop
    }

  def relaying: Receive = LoggingReceive {
    case Notifier.Notify(auction, buyer, price) =>
      val request = context.actorOf(Props[NotifierRequest])
      request ! Notifier.Notify(auction, buyer, price)
  }
  def receive = relaying
}
