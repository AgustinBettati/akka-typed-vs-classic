package supervision.typed

import akka.actor.typed.{ActorRef, Behavior, DeathPactException, SupervisorStrategy, Terminated}
import akka.actor.typed.scaladsl.Behaviors

// This can be seen similar to Escalate in akka classic

object Protocol {
  sealed trait Command
  case class Fail(text: String) extends Command
  case class Hello(text: String, replyTo: ActorRef[String]) extends Command
}
import Protocol._

object Worker {
  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {
      case Fail(text) =>
        throw new RuntimeException(text)
      case Hello(text, replyTo) =>
        replyTo ! text
        Behaviors.same
    }
}

object Boss {
  def apply(): Behavior[Command] =
    Behaviors.setup[Command] { context =>

      context.log.info("Middle management starting up")
      // default supervision of child, meaning that it will stop on failure
      val child: ActorRef[Command] = context.spawn(Worker(), "child")
      // we want to know when the child terminates, but since we do not handle
      // the Terminated signal, we will in turn fail on child termination
      context.watch(child)

      Behaviors.receiveMessage[Command] { message =>
        child ! message
        Behaviors.same
      }.receiveSignal {
        case (_, Terminated(ref)) =>
          context.log.info("handling termination of child actor")
          Behaviors.same
      }
    }
}
