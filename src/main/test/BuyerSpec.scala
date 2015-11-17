package main.test

import akka.actor.{ActorRefFactory, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import main.Auction.Bid
import main.Buyer.{Gazump, InitAggressive}
import main.{AuctionSearch, Buyer, Auction, Seller}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.collection.mutable

class BuyerSpec extends TestKit(ActorSystem("BuyerSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.shutdown()
  }

  "An Aggressive Buyer" must {
    "fight to the end" in {

      val probe = TestProbe()
      val buyer = system.actorOf(Props[Buyer], "buyer1")
      buyer ! InitAggressive(100)
      probe.send(buyer, Gazump(2))
      probe.expectMsg(Bid(3))
    }
  }
}