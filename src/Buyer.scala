import java.util.Random

import Buyer.{BuyerData, BuyerState}
import akka.actor.{FSM, Actor, ActorRef}

import scala.collection.mutable.MutableList

object Buyer {

  sealed trait BuyerMessage

  case class Init(auctions: MutableList[ActorRef]) extends BuyerMessage

  case object Bid extends BuyerMessage

  case object BidAccepted extends BuyerMessage

  case object BidRejected extends BuyerMessage

  case object Won extends BuyerMessage

  sealed trait BuyerState

  case object Uninitialized extends BuyerState

  case object Initialized extends BuyerState

  sealed trait BuyerData

  case object UnunitializedD extends BuyerData

  case class InitializedD(auctions: MutableList[ActorRef]) extends BuyerData

}

class Buyer extends Actor with FSM[BuyerState, BuyerData] {
  import Buyer._
  val rand = new Random()

  startWith(Uninitialized, UnunitializedD)

  when(Uninitialized) {
    case Event(Init(auctions), _) =>
      goto(Initialized) using InitializedD(auctions)
  }

  when(Initialized) {
    case Event(Bid, InitializedD(auctions)) =>
      val amount = rand.nextInt(100)
      val index = rand.nextInt(`auctions`.length)
      println(self + " bid " + `auctions`(index) + " with " + amount.toString())
      `auctions`(index) ! Auction.Bid(amount)
      stay
    case Event(BidAccepted, _) =>
      stay
    case Event(BidRejected, _) =>
      stay
    case Event(Won, _) =>
      println(self + " won " + sender)
      stop()
  }

  initialize()
}
