import java.util.concurrent.TimeUnit

import Auction.{AuctionData, AuctionState}
import akka.actor._
import scala.concurrent.duration._

object Auction {

  sealed trait AuctionMessage

  final case class Create(auctionLength: Int) extends AuctionMessage

  final case class Bid(amount: BigInt) extends AuctionMessage

  case object BidExpire extends AuctionMessage

  case object DeleteExpire extends AuctionMessage

  case object Relist extends AuctionMessage


  sealed trait AuctionState

  case object Created extends AuctionState

  case object Ignored extends AuctionState

  case object Activated extends AuctionState

  case object Uninitialized extends AuctionState

  case object Sold extends AuctionState


  sealed trait AuctionData

  case object UninitializedD extends AuctionData

  final case class ActivatedD(buyer: ActorRef, currentPrice: BigInt) extends AuctionData

}

class Auction extends Actor with FSM[AuctionState, AuctionData] {
  import Auction._

  startWith(Uninitialized, UninitializedD)

  when(Uninitialized) {
    case Event(Create, _) =>
      goto(Created)
  }

  when(Created, stateTimeout = 5 seconds) {
    case Event(Bid(amount), _) =>
      goto(Activated) using ActivatedD(sender(), amount)
    case Event(StateTimeout, _) =>
      goto(Ignored)
  }

  when(Ignored, stateTimeout = 5 seconds) {
    case Event(Relist, _) =>
      goto(Created)
    case Event(StateTimeout, _) =>
      stop()
  }

  when(Activated, stateTimeout = 5 seconds) {
    case Event(Bid(amount), ActivatedD(buyer, currentPrice)) =>
      if(amount > `currentPrice`) {
        sender() ! Buyer.BidAccepted
        goto(Activated) using ActivatedD(sender(), amount)
      } else {
        sender() ! Buyer.BidRejected
        stay
      }
    case Event(StateTimeout, ActivatedD(buyer, currentPrice)) =>
      `buyer` ! Buyer.Won
      goto(Sold)
  }

  when(Sold, stateTimeout = 5 seconds) {
    case Event(StateTimeout, _) =>
      stop()
  }

  initialize()
}