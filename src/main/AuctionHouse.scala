package main

import akka.actor.{ActorRefFactory, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.MutableList

object AuctionHouse extends App {

  val config = ConfigFactory.load()
  val publisherSystem = ActorSystem("AuctionHouse", config.getConfig("publisherapp").withFallback(config))
  val publisher = publisherSystem.actorOf(Props[AuctionPublisher], "publisher")


  val system = ActorSystem("Reactive2")
  val notifier = system.actorOf(Props[Notifier], "notifier")
  val buyer1 = system.actorOf(Props[Buyer], "buyer1")
  val buyer2 = system.actorOf(Props[Buyer], "buyer2")
  val maker = (f: ActorRefFactory) => f.actorOf(Props[Auction])
  val seller1 = system.actorOf(Props(classOf[Seller], maker), "seller1")
  val seller2 = system.actorOf(Props(classOf[Seller], maker), "seller2")
  val auctionSearch = system.actorOf(Props[MasterSearch], "auctionSearch")
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