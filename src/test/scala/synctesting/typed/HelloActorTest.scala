package synctesting.typed
import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.Effect._
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.testkit.typed.scaladsl.TestInbox
import akka.actor.typed._
import akka.actor.typed.scaladsl._
import com.typesafe.config.ConfigFactory
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.event.Level

class HelloActorTest extends AnyWordSpecLike {

  "A HelloActor" should {
    "spawn a new child" in {
      val testKit = BehaviorTestKit(HelloActor())
      testKit.run(HelloActor.CreateChild("child"))
      testKit.expectEffect(Spawned(HelloActor.childActor, "child"))
    }

    "spawn a new anonymous child" in {
      val testKit = BehaviorTestKit(HelloActor())
      testKit.run(HelloActor.CreateAnonymousChild)
      testKit.expectEffect(SpawnedAnonymous(HelloActor.childActor))
    }

    "send a message" in {
      val testKit = BehaviorTestKit(HelloActor())
      val inbox = TestInbox[String]()
      testKit.run(HelloActor.SayHello(inbox.ref))
      inbox.expectMessage("hello")
    }

  }


}
