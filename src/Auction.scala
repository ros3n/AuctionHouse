import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.event.LoggingReceive
import scala.collection.mutable.MutableList
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import java.util.Random

object Buyer {

  sealed trait BuyerMessage

  case class Init(auctions: MutableList[ActorRef]) extends BuyerMessage

  case object Bid extends BuyerMessage

  case object BidAccepted extends BuyerMessage

  case object BidRejected extends BuyerMessage

  case object Won extends BuyerMessage

}

class Buyer extends Actor {
  import Buyer._
  val rand = new Random()

  def uninitialized: Receive = LoggingReceive {
    case Init (auctions) =>
      context become initialized(auctions)
  }

  def initialized(auctions: MutableList[ActorRef]): Receive = LoggingReceive {
    case Bid =>
      val amount = rand.nextInt(100)
      val index = rand.nextInt(`auctions`.length)
      `auctions`(index) ! Auction.Bid(amount)
    case BidAccepted =>
    case BidRejected =>
    case Won =>
      println(self + " won " + sender)
  }

  def receive = uninitialized
}

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

object AuctionHouse extends App {
  val system = ActorSystem("Reactive2")
  val auction = system.actorOf(Props[Auction], "auction1")
  val auction2 = system.actorOf(Props[Auction], "auction2")
  val auction3 = system.actorOf(Props[Auction], "auction3")
  val buyer1 = system.actorOf(Props[Buyer], "buyer1")
  val buyer2 = system.actorOf(Props[Buyer], "buyer2")
  val auctions =  MutableList(auction, auction2, auction3)
  auction ! Auction.Create(5000)
  auction2 ! Auction.Create(5000)
  auction3 ! Auction.Create(5000)
  Thread.sleep(1000)
  buyer1 ! Buyer.Init(auctions)
  buyer2 ! Buyer.Init(auctions)
  buyer1 ! Buyer.Bid
  buyer2 ! Buyer.Bid
  buyer1 ! Buyer.Bid
  Thread.sleep(1000)
  buyer2 ! Buyer.Bid
  buyer1 ! Buyer.Bid
  buyer2 ! Buyer.Bid
  buyer1 ! Buyer.Bid
  buyer2 ! Buyer.Bid


  system.awaitTermination()
}