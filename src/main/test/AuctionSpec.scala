package main

import main.Auction.{Bid, AskName, Create}
import AuctionSearch.Register
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import main.Buyer.{BidRejected, BidAccepted}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}


class AuctionSpec extends TestKit(ActorSystem("AuctionSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.shutdown()
  }

  "An Auction" must {
    "be createable" in {

      val auctionSearchProbe = TestProbe("auctionSearch")
      val seller = TestProbe("seller")
      val auction = system.actorOf(Props[Auction], "auction")
      auction ! Create("auction name", seller.ref, 50000, System.currentTimeMillis / 1000)
    }

    "tell it's name" in {
      val auction = system.actorOf(Props[Auction], "auction")
      val seller = TestProbe("seller")
      val probe = TestProbe()
      auction ! Create("auction name", seller.ref, 50000, System.currentTimeMillis / 1000)

      probe.send(auction, AskName)
      probe.expectMsg("auction name")
    }

    "accept and reject bids" in {
      val auction = system.actorOf(Props[Auction], "auction")
      val seller = TestProbe("seller")
      val probe = TestProbe()
      auction ! Create("auction name", seller.ref, 50000, System.currentTimeMillis / 1000)
      auction ! Bid(1)
      probe.send(auction, Bid(100))
      probe.expectMsg(BidAccepted)
      probe.send(auction, Bid(50))
      probe.expectMsg(BidRejected)
    }
  }
}