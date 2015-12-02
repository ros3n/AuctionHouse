package main


import akka.actor.{ActorRefFactory, ActorSystem, Props}
import akka.util.Timeout
import main.Seller.Init

import scala.collection.mutable.MutableList

import scala.concurrent.Await
import akka.pattern.ask
import scala.concurrent.duration._
import scala.util.Random

object AuctionHouse extends App {

//  val config = ConfigFactory.load()
//  val publisherSystem = ActorSystem("AuctionHouse", config.getConfig("publisherapp").withFallback(config))
//  val publisher = publisherSystem.actorOf(Props[AuctionPublisher], "publisher")


//  val system = ActorSystem("Reactive2")
//  val notifier = system.actorOf(Props[Notifier], "notifier")
//  val buyer1 = system.actorOf(Props[Buyer], "buyer1")
//  val buyer2 = system.actorOf(Props[Buyer], "buyer2")
//  val maker = (f: ActorRefFactory) => f.actorOf(Props[Auction])
//  val seller1 = system.actorOf(Props(classOf[Seller], maker), "seller1")
//  val seller2 = system.actorOf(Props(classOf[Seller], maker), "seller2")
//  val auctionSearch = system.actorOf(Props[MasterSearch], "auctionSearch")
//  seller1 ! Seller.Init(MutableList[String]("audi a6 diesel auto"))
//  seller1 ! Seller.Init(MutableList[String]("chevy camaro v8 manual"))
//  seller2 ! Seller.Init(MutableList[String]("yummy donuts"))
//  buyer1 ! Buyer.Init
//  buyer2 ! Buyer.Init
//  Thread.sleep(1000)
//  buyer1 ! Buyer.Search("audi")
//  buyer1 ! Buyer.Search("v8")
//  buyer2 ! Buyer.Search("manual")
//  buyer2 ! Buyer.Search("donuts")
//  Thread.sleep(2000)
//  buyer1 ! Buyer.Bid
//  buyer2 ! Buyer.Bid
//  buyer1 ! Buyer.Bid
//  Thread.sleep(1000)
//  buyer2 ! Buyer.Bid
//  buyer1 ! Buyer.Bid
//  buyer2 ! Buyer.Bid
//  buyer1 ! Buyer.Bid
//  buyer2 ! Buyer.Bid

    val system = ActorSystem("Reactive2")
    val auctionSearch = system.actorOf(Props[MasterSearch], "auctionSearch")
    val maker = (f: ActorRefFactory) => f.actorOf(Props[Auction])
    val seller = system.actorOf(Props(classOf[Seller], maker), "seller")
    var list = MutableList[String]()
    val n = 50000
    1 to n foreach { _ =>
      list += Random.alphanumeric.take(10).mkString("").toString()
    }
    seller ! Init(list)

    var res = 0

    while(res < 5 * n) {
      implicit val timeout = Timeout(5 seconds)
      val future = auctionSearch ? "how many?"
      res = Await.result(future, timeout.duration).asInstanceOf[Int]
      println(res)
      Thread.sleep(5000)
    }

    println(res)

    val buyer = system.actorOf(Props[Buyer], "buyer")
    buyer ! Init

    val rand = new java.util.Random()

    val start = System.nanoTime()

    1 to 10000 foreach { _ =>
      buyer ! Buyer.Search(list.get(rand.nextInt(n)).toString)
    }

    println("Done:" + (System.nanoTime() - start))

  system.awaitTermination()
}