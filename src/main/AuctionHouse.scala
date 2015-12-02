package main

import akka.actor.{ActorRefFactory, ActorSystem, Props}

import scala.collection.mutable.MutableList

object AuctionHouse extends App {
  val system = ActorSystem("Reactive2")
  val buyer1 = system.actorOf(Props[Buyer], "buyer1")
  val maker = (f: ActorRefFactory) => f.actorOf(Props[Auction])
  val seller1 = system.actorOf(Props(classOf[Seller], maker), "seller1")
  val auctionSearch = system.actorOf(Props[AuctionSearch], "auctionSearch")
  seller1 ! Seller.Init(MutableList[String]("audi a6 diesel auto"))
  buyer1 ! Buyer.Init
  Thread.sleep(1000)
  buyer1 ! Buyer.Search("audi")
  Thread.sleep(3000)
  buyer1 ! Buyer.Bid
  Thread.sleep(3000)
  buyer1 ! Buyer.Bid

  system.awaitTermination()
}