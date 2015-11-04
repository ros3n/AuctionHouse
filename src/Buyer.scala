import java.util.Random

import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

import scala.collection.mutable.MutableList

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
