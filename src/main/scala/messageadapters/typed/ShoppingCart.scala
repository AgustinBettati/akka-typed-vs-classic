package messageadapters.typed

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

case class Product(name: String, price: Double)

object ShoppingCart {

  sealed trait Request
  case class GetCurrentCart(cartId: String, replyTo: ActorRef[Response]) extends Request
  // some others

  sealed trait Response
  case class CurrentCart(cartId: String, items: List[Product]) extends Response
  // some others

  // NEW: a dummy database holding all the current shopping carts
  val db: Map[String, List[Product]] = Map {
    "123-abc-456" -> List(Product("iPhone", 7000), Product("selfie stick", 30))
  }

  // NEW: a dummy shopping cart fetching things from the internal in-memory "database"/map
  def apply(): Behavior[Request] = Behaviors.receiveMessage {
    case GetCurrentCart(cartId, replyTo) =>
      replyTo ! CurrentCart(cartId, db(cartId))
      Behaviors.same
  }
}
