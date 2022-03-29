package messageadapters.typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, DispatcherSelector, Dispatchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object AkkaMessageAdaptation {

  def main(args: Array[String]): Unit = {
    import Checkout._

    val rootBehavior: Behavior[Any] = Behaviors.setup { context =>
      val shoppingCart = context.spawn(ShoppingCart(), "shopping-cart")

      // simple customer actor displaying the total amount due
      val customer = context.spawn(Behaviors.receiveMessage[Response] {
        case Summary(_, amount) =>
          println(s"Total to pay: $amount - pay by card below.")
          Behaviors.same
      }, "customer")

      val checkout = context.spawn(Checkout(shoppingCart), "checkout")

      // trigger an interaction
      checkout ! InspectSummary("123-abc-456", customer)

      // no behavior for the actor system
      Behaviors.empty
    }

    // setup/teardown
    val system = ActorSystem(rootBehavior, "main-app")
    implicit val ec: ExecutionContext = system.dispatchers.lookup(DispatcherSelector.default)
    system.scheduler.scheduleOnce(1.second, () => system.terminate())
  }
}
