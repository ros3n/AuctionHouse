package main

import java.util.Random

import AuctionSearch.SearchQuery
import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.mutable.MutableList
import scala.concurrent.Await
import scala.concurrent.duration._

object Buyer {

  sealed trait BuyerMessage

  case object Init extends BuyerMessage

  case object Bid extends BuyerMessage

  case object BidAccepted extends BuyerMessage

  case object BidRejected extends BuyerMessage

  case class Won(name: String) extends BuyerMessage

  case class Search(query: String)

}

class Buyer extends Actor {
  import Buyer._
  val rand = new Random()

  def uninitialized: Receive = LoggingReceive {
    case Init =>
      context become initialized(MutableList[ActorRef]())
  }

  def initialized(auctions: MutableList[ActorRef]): Receive = LoggingReceive {
    case Bid =>
      if(`auctions`.length > 0) {
        val amount = rand.nextInt(100)
        val index = rand.nextInt(`auctions`.length)
        `auctions`(index) ! Auction.Bid(amount)
      }
    case BidAccepted =>
    case BidRejected =>
    case Won(name) =>
      println(self + " won " + name)
    case Search(query) =>
      implicit val timeout = Timeout(5 seconds)
      val future = context.actorSelection("/user/auctionSearch") ? SearchQuery(query)
      val result = Await.result(future, timeout.duration).asInstanceOf[MutableList[ActorRef]]
      if(result.length > 0) {
        context become initialized(`auctions` ++ result)
      }
  }

  def receive = uninitialized
}
