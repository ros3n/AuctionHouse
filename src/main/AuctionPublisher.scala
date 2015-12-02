package main

import akka.actor.Actor
import akka.event.LoggingReceive

object AuctionPublisher {
  sealed trait AuctionPublisherMessage

  case class Publish(auctionName: String, buyer: String, price: BigInt) extends AuctionPublisherMessage
}


class AuctionPublisher extends Actor {

  def logging: Receive = LoggingReceive {
    case AuctionPublisher.Publish(auctionName, buyer, price) =>
      println("###### " + auctionName + ": " + buyer + ", " + price.toString())
  }
  def receive = logging
}
