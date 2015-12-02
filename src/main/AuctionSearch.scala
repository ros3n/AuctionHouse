package main

import Auction.AskName
import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive
import akka.util.Timeout

import scala.collection.mutable.MutableList
import scala.concurrent.Await
import akka.pattern.ask
import scala.concurrent.duration._

object AuctionSearch {
  sealed trait AuctionSearchMessage
  case class Register(auction: ActorRef)
  case class SearchQuery(query: String)
}

class AuctionSearch extends Actor {
  import AuctionSearch._

  def searchable(auctions: MutableList[ActorRef]): Receive = LoggingReceive {
    case Register(auction) =>
      context.actorSelection("/user/auctionSearch") ! "done!"
      context become searchable(`auctions` += auction)
    case SearchQuery(query) =>
      val result = MutableList[ActorRef]()
      `auctions`.foreach { auction =>
        implicit val timeout = Timeout(5 seconds)
        val future = auction ? AskName
        val name = Await.result(future, timeout.duration).asInstanceOf[String]
        if(name.contains(query)) {
          result += auction
        }
      }
      sender() ! result
  }

  def receive = searchable(MutableList[ActorRef]())
}
