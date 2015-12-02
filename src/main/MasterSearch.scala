package main

import akka.actor.{Props, Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.routing._
import main.AuctionSearch.{SearchQuery, Register}

class MasterSearch extends Actor with ActorLogging {
  val nbOfroutees: Int = 5
  var counter: Int = 0

  val routees = Vector.fill(nbOfroutees) {
    val r = context.actorOf(Props[AuctionSearch])
    context watch r
    ActorRefRoutee(r)
  }

  var broadcastRouter = {
    Router(BroadcastRoutingLogic(), routees)
  }

  var roundRobinRouter = {
//    Router(RoundRobinRoutingLogic(), routees)
    Router(SmallestMailboxRoutingLogic(), routees)
  }

  def receive = LoggingReceive {
    case Register(auction) =>
      broadcastRouter.route(Register(auction), sender())
    case SearchQuery(query) =>
      roundRobinRouter.route(SearchQuery(query), sender())
    case "done!" =>
      counter += 1
    case "how many?" =>
      sender() ! counter
  }
}
