import akka.actor.{Props, ActorSystem}

import scala.collection.mutable.MutableList

object AuctionHouse extends App {
  val system = ActorSystem("Reactive2")
  val auction = system.actorOf(Props[Auction], "auction1")
  val auction2 = system.actorOf(Props[Auction], "auction2")
  val auction3 = system.actorOf(Props[Auction], "auction3")
  val buyer1 = system.actorOf(Props[Buyer], "buyer1")
  val buyer2 = system.actorOf(Props[Buyer], "buyer2")
  val auctions = MutableList(auction, auction2, auction3)
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