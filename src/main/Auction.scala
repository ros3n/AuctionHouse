package main

import java.util.concurrent.TimeUnit

import AuctionSearch.Register
import Seller.AuctionSold
import akka.event.LoggingReceive
import akka.persistence.PersistentActor
import main.Buyer.Gazump
import scala.concurrent.duration._
import akka.actor.ActorRef

object Auction {

  sealed trait AuctionMessage

  case class Create(name: String, seller: ActorRef, auctionLength: Int, timestamp: Long) extends AuctionMessage

  case class Bid(amount: BigInt) extends AuctionMessage

  case object BidExpire extends AuctionMessage

  case object DeleteExpire extends AuctionMessage

  case object Relist extends AuctionMessage

  case object AskName extends AuctionMessage
}

class Auction extends PersistentActor {
  import Auction._
  import context._
  import context.dispatcher

  override def persistenceId = "persistent-auction-id-1"

  def uninitialized: Receive = LoggingReceive {
    case Create(name, seller, auctionLength, timestamp) =>
      persist(Create(name, seller, auctionLength, timestamp)) { ev =>
        context.actorSelection("/user/auctionSearch") ! Register(self)
        system.scheduler.scheduleOnce(new FiniteDuration(`auctionLength`, TimeUnit.MILLISECONDS), self, BidExpire)
        context become created(`name`, `seller`, `auctionLength`)
      }
  }

  def created(name: String, seller: ActorRef, auctionLength: Int): Receive = LoggingReceive {
    case Bid(amount) =>
      persist(Bid(amount)) { ev =>
        context become activated(`name`, `seller`, sender(), amount)
      }
    case BidExpire =>
      persist(BidExpire) { ev =>
        system.scheduler.scheduleOnce(5000 millis, self, DeleteExpire)
        context become ignored(`name`, `seller`, `auctionLength`)
      }
    case AskName =>
      sender() ! `name`
  }

  def ignored(name: String, seller: ActorRef, auctionLength: Int): Receive = LoggingReceive {
    case Relist =>
      persist(Relist) { ev =>
        system.scheduler.scheduleOnce(new FiniteDuration(`auctionLength`, TimeUnit.MILLISECONDS), self, BidExpire)
        context become created(`name`, `seller`, `auctionLength`)
      }
    case DeleteExpire =>
      context.stop(self)
    case AskName =>
      sender() ! `name`
  }

  def activated(name: String, seller: ActorRef, buyer: ActorRef, currentPrice: BigInt): Receive = LoggingReceive {
    case Bid(amount) if amount > `currentPrice` =>
      persist(Bid(amount)) { ev =>
        sender() ! Buyer.BidAccepted
        `buyer` ! Gazump(amount)
        context become activated(`name`, `seller`, sender(), amount)
      }
    case Bid(amount) if amount <= `currentPrice` =>
      sender() ! Buyer.BidRejected
    case BidExpire =>
      persist(BidExpire) { ev =>
        `seller` ! AuctionSold(`name`)
        `buyer` ! Buyer.Won(`name`)
        system.scheduler.scheduleOnce(5000 millis, self, DeleteExpire)
        context become sold
      }
    case AskName =>
      sender() ! `name`
  }

  def sold: Receive = LoggingReceive {
    case DeleteExpire =>
      context.stop(self)
  }

  def receiveCommand = uninitialized

  val receiveRecover: Receive = {
    case Create(name, seller, auctionLength, timestamp) => {
      self ! Create(name, seller, (auctionLength + timestamp - System.currentTimeMillis).toInt, System.currentTimeMillis)
    }
    case evt: AuctionMessage => self ! evt
  }
}