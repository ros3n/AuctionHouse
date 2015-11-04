import akka.actor.{Props, ActorSystem}

import scala.collection.mutable.MutableList

object AuctionHouse extends App {
  val system = ActorSystem("Reactive2")
  val buyer1 = system.actorOf(Props[Buyer], "buyer1")
  val buyer2 = system.actorOf(Props[Buyer], "buyer2")
  val seller1 = system.actorOf(Props[Seller], "seller1")
  val seller2 = system.actorOf(Props[Seller], "seller2")
  val auctionSearch = system.actorOf(Props[AuctionSearch], "auctionSearch")
  seller1 ! Seller.Init(MutableList[String]("audi a6 diesel auto"))
  seller1 ! Seller.Init(MutableList[String]("chevy camaro v8 manual"))
  seller2 ! Seller.Init(MutableList[String]("yummy donuts"))
  buyer1 ! Buyer.Init
  buyer2 ! Buyer.Init
  Thread.sleep(1000)
  buyer1 ! Buyer.Search("audi")
  buyer1 ! Buyer.Search("v8")
  buyer2 ! Buyer.Search("manual")
  buyer2 ! Buyer.Search("donuts")
  Thread.sleep(2000)
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