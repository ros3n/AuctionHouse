package main

import akka.actor.{Props, Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.routing.{RoundRobinRoutingLogic, BroadcastRoutingLogic, Router, ActorRefRoutee}
import main.AuctionSearch.{SearchQuery, Register}

class MasterSearch extends Actor with ActorLogging {
  val nbOfroutees: Int = 5

  val routees = Vector.fill(nbOfroutees) {
    val r = context.actorOf(Props[AuctionSearch])
    context watch r
    ActorRefRoutee(r)
  }

  var broadcastRouter = {
    Router(BroadcastRoutingLogic(), routees)
  }

  var roundRobinRouter = {
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = LoggingReceive {
    case Register(auction) =>
      broadcastRouter.route(Register(auction), sender())
    case SearchQuery(query) =>
      roundRobinRouter.route(SearchQuery(query), sender())
  }
}
