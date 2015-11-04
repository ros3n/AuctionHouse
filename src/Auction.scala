import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.event.LoggingReceive
import scala.collection.mutable.MutableList
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import java.util.Random

object Auction {

  sealed trait AuctionMessage

  case class Create(auctionLength: Int) extends AuctionMessage

  case class Bid(amount: BigInt) extends AuctionMessage

  case object BidExpire extends AuctionMessage

  case object DeleteExpire extends AuctionMessage

  case object Relist extends AuctionMessage

}

class Auction extends Actor {
  import Auction._
  import context._
  import context.dispatcher

  def uninitialized: Receive = LoggingReceive {
    case Create(auctionLength) =>
      system.scheduler.scheduleOnce(new FiniteDuration(`auctionLength`, TimeUnit.MILLISECONDS) , self, BidExpire)
      context become created(`auctionLength`)
  }

  def created(auctionLength: Int): Receive = LoggingReceive {
    case Bid(amount) =>
      context become activated(sender(), amount)
    case BidExpire =>
      system.scheduler.scheduleOnce(5000 millis, self, DeleteExpire)
      context become ignored(`auctionLength`)
  }

  def ignored(auctionLength: Int): Receive = LoggingReceive {
    case Relist =>
      system.scheduler.scheduleOnce(new FiniteDuration(`auctionLength`, TimeUnit.MILLISECONDS) , self, BidExpire)
      context become created(`auctionLength`)
    case DeleteExpire =>
      context.stop(self)
  }

  def activated(buyer: ActorRef, currentPrice: BigInt): Receive = LoggingReceive {
    case Bid(amount) if amount > `currentPrice` =>
      sender() ! Buyer.BidAccepted
      context become activated(sender(), amount)
    case Bid(amount) if amount <= `currentPrice` =>
      sender() ! Buyer.BidRejected
    case BidExpire =>
      `buyer` ! Buyer.Won
      system.scheduler.scheduleOnce(5000 millis, self, DeleteExpire)
      context become sold
  }

  def sold: Receive = LoggingReceive {
    case DeleteExpire =>
      context.stop(self)
  }

  def receive = uninitialized
}