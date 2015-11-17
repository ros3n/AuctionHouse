import Auction.Create
import akka.actor.{ActorRefFactory, Props, ActorRef, Actor}
import akka.event.LoggingReceive

import scala.collection.mutable.MutableList

object Seller {
  sealed trait SellerMessage
  case class Init(auctionNames: MutableList[String]) extends SellerMessage
  case object CreateAuctions
  case class AuctionSold(name: String)
}

class Seller(auctionMaker: ActorRefFactory => ActorRef) extends Actor {
  import Seller._

  def uninitialized: Receive = LoggingReceive {
    case Init(auctionNames) =>
      val auctions = MutableList[ActorRef]()
      auctionNames.foreach { name =>
        val auction = auctionMaker(context)
        auction ! Create(name, self, 10000)
        auctions += auction
      }
    case AuctionSold(name) =>
      println(self + " sold " + name)
  }

  def receive = uninitialized
}
