package actordiscovery.typed

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors

object PingService {
  val PingServiceKey: ServiceKey[Ping] = ServiceKey[Ping]("pingService")

  final case class Ping(replyTo: ActorRef[Pong.type])
  case object Pong

  def apply(): Behavior[Ping] = {
    Behaviors.setup { context =>
      context.system.receptionist ! Receptionist.Register(PingServiceKey, context.self)

      Behaviors.receiveMessage {
        case Ping(replyTo) =>
          context.log.info("Pinged by {}", replyTo)
          replyTo ! Pong
          Behaviors.same
      }
    }
  }
}

object Pinger {
  def apply(pingService: ActorRef[PingService.Ping]): Behavior[PingService.Pong.type] = {
    Behaviors.setup { context =>
      pingService ! PingService.Ping(context.self)
      Behaviors.receiveMessage { _ =>
        context.log.info("{} was ponged!!", context.self)
        Behaviors.stopped
      }
    }
  }
}

object Guardian {
  def apply(): Behavior[Receptionist.Listing] = {
    Behaviors.setup[Receptionist.Listing] { context =>
        context.spawnAnonymous(PingService())
        context.system.receptionist ! Receptionist.Subscribe(PingService.PingServiceKey, context.self)

        Behaviors.receiveMessagePartial[Receptionist.Listing] {
          case PingService.PingServiceKey.Listing(listings) => // new registered ping services
            listings.foreach(ps => context.spawnAnonymous(Pinger(ps)))
            Behaviors.same
        }
      }
  }
}

object Main extends App {
  private val system: ActorSystem[Nothing] = ActorSystem(Guardian(), "main")
  Thread.sleep(1000)
  system.terminate()
}
