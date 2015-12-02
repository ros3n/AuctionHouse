import akka.actor.{ActorRefFactory, Props, ActorSystem}
import akka.testkit.{TestProbe, ImplicitSender, TestKit}
import main.{Auction, Seller}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.collection.mutable

class SellerSpec extends TestKit(ActorSystem("SellerSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.shutdown()
  }

  "A Seller" must {
    "create auctions" in {

      val probe = TestProbe()
      val maker = (_: ActorRefFactory) => probe.ref

      val seller = system.actorOf(Props(classOf[Seller], maker), "seller")
      seller ! Seller.Init(mutable.MutableList[String]("yummy donuts"))

      probe.expectMsg(Auction.Create("yummy donuts", seller, 10000, System.currentTimeMillis / 1000))
    }
  }
}