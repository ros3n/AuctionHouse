package main

import java.util.concurrent.TimeUnit

import AuctionSearch.Register
import Seller.AuctionSold
import akka.actor.Actor
import akka.event.LoggingReceive
import main.Notifier.Notify
import scala.concurrent.duration._
import akka.actor.ActorRef

object Auction {

  sealed trait AuctionMessage

  case class Create(name: String, seller: ActorRef, auctionLength: Int) extends AuctionMessage

  case class Bid(amount: BigInt) extends AuctionMessage

  case object BidExpire extends AuctionMessage

  case object DeleteExpire extends AuctionMessage

  case object Relist extends AuctionMessage

  case object AskName extends AuctionMessage

}

class Auction extends Actor {
  import Auction._
  import context._
  import context.dispatcher

  def uninitialized: Receive = LoggingReceive {
    case Create(name, seller, auctionLength) =>
      context.actorSelection("/user/auctionSearch") ! Register(self)
      system.scheduler.scheduleOnce(new FiniteDuration(`auctionLength`, TimeUnit.MILLISECONDS) , self, BidExpire)
      context become created(`name`, `seller`, `auctionLength`)
  }

  def created(name: String, seller: ActorRef, auctionLength: Int): Receive = LoggingReceive {
    case Bid(amount) =>
      context.actorSelection("/user/notifier") ! Notify(self, sender(), amount)
      context become activated(`name`, `seller`, sender(), amount)
    case BidExpire =>
      system.scheduler.scheduleOnce(5000 millis, self, DeleteExpire)
      context become ignored(`name`, `seller`, `auctionLength`)
    case AskName =>
      sender() ! `name`
  }

  def ignored(name: String, seller: ActorRef, auctionLength: Int): Receive = LoggingReceive {
    case Relist =>
      system.scheduler.scheduleOnce(new FiniteDuration(`auctionLength`, TimeUnit.MILLISECONDS) , self, BidExpire)
      context become created(`name`, `seller`, `auctionLength`)
    case DeleteExpire =>
      context.stop(self)
  }

  def activated(name: String, seller: ActorRef, buyer: ActorRef, currentPrice: BigInt): Receive = LoggingReceive {
    case Bid(amount) if amount > `currentPrice` =>
      sender() ! Buyer.BidAccepted
      context.actorSelection("/user/notifier") ! Notify(self, sender(), amount)
      context become activated(`name`, `seller`, sender(), amount)
    case Bid(amount) if amount <= `currentPrice` =>
      sender() ! Buyer.BidRejected
    case BidExpire =>
      `seller` ! AuctionSold(`name`)
      `buyer` ! Buyer.Won(`name`)
      system.scheduler.scheduleOnce(5000 millis, self, DeleteExpire)
      context become sold
  }

  def sold: Receive = LoggingReceive {
    case DeleteExpire =>
      context.stop(self)
  }

  def receive = uninitialized
}