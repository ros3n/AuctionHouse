package main

import akka.actor.{ActorRef, Actor}
import akka.event.LoggingReceive


class NotifierRequest extends Actor {
  def relaying: Receive = LoggingReceive {
    case Notifier.Notify(auction, buyer, price) =>
      val publisher = context.actorSelection("akka.tcp://AuctionHouse@127.0.0.1:2555/user/publisher")
      publisher ! AuctionPublisher.Publish(auction.toString(), buyer.toString(), price)
  }
  def receive = relaying
}
