package main.test

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import main.Auction.{AskName, Bid, Create}
import main.AuctionSearch.{SearchQuery, Register}
import main.{Seller, Auction, AuctionSearch}
import main.Buyer.{BidAccepted, BidRejected}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.collection.mutable


class AuctionSearchSpec extends TestKit(ActorSystem("AuctionSearchSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.shutdown()
  }

  "An AuctionSearch" must {
    "be searchable" in {


      val auctionSearch = system.actorOf(Props[AuctionSearch], "auctionSearch")
      val auction1 = system.actorOf(Props[Auction], "auction1")
      val auction2 = system.actorOf(Props[Auction], "auction2")
      val seller = TestProbe()

      auction1 ! Create("auction 1 name", seller.ref, 100000, System.currentTimeMillis / 1000)
      auction2 ! Create("auction 2 name", seller.ref, 10000, System.currentTimeMillis / 1000)

      auctionSearch ! Register(auction1)
      auctionSearch ! Register(auction2)

      val probe = TestProbe()
      probe.send(auctionSearch, SearchQuery("1 name"))
      probe.expectMsg(mutable.MutableList[ActorRef](auction1))
    }
  }
}