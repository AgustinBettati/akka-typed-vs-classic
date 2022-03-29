package messageadapters.typed

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Checkout {
  import ShoppingCart._

  sealed trait Request
  final case class InspectSummary(cartId: String, replyTo: ActorRef[Response]) extends Request
  // some others

  // message wrapper that can translate from the outer (backend) actor's responses to my own useful data structures
  private final case class WrappedSCResponse(response: ShoppingCart.Response) extends Request

  sealed trait Response
  final case class Summary(cartId: String, amount: Double) extends Response

  def apply(shoppingCart: ActorRef[ShoppingCart.Request]): Behavior[Request] =
    Behaviors.setup[Request] { context =>
      // adapter goes here
      val responseMapper: ActorRef[ShoppingCart.Response] =
        context.messageAdapter(rsp => WrappedSCResponse(rsp))

      // checkout behavior's logic
      def handlingCheckouts(checkoutsInProgress: Map[String, ActorRef[Response]]): Behavior[Request] = {
        Behaviors.receiveMessage[Request] {
          // message from customer - query the shopping cart
          // the recipient of that response is my message adapter
          case InspectSummary(cartId, replyTo) =>
            shoppingCart ! ShoppingCart.GetCurrentCart(cartId, responseMapper) // <--- message adapter here
            handlingCheckouts(checkoutsInProgress + (cartId -> replyTo))

          // the wrapped message from my adapter: deal with the Shopping Cart's response here
          case WrappedSCResponse(resp) =>
            resp match {
              case CurrentCart(cartId, items) =>
                val summary = Summary(cartId, items.map(_.price).sum)
                val customer = checkoutsInProgress(cartId)
                customer ! summary
                Behaviors.same

              // handle other potential responses from the ShoppingCart actor here
            }

        }
      }

      handlingCheckouts(checkoutsInProgress = Map())
    }
}
