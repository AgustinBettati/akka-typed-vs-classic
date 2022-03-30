package statefulactor.typed

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import statefulactor.typed.TypedStatefulActor.{Add, Command, Count, GetCount}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.concurrent.Future
import scala.concurrent.duration._

object TypedStatefulActor {

  sealed trait Command
  case object Add extends Command
  case class GetCount(replyTo: ActorRef[Count]) extends Command

  case class Count(num: Int)

  def apply(): Behavior[Command] = counter(0)

  def counter(count: Int): Behavior[Command] = Behaviors.receiveMessage {
    case Add => counter(count + 1)
    case GetCount(replyTo: ActorRef[Count]) =>
      replyTo ! Count(count)
      Behaviors.same
  }

}

object TypedStatefulActorRunner extends App {

  val system: ActorSystem[Command] = ActorSystem(TypedStatefulActor(),"counter")

  system ! Add
  system ! Add
  system ! Add
  Thread.sleep(200)

  implicit val scheduler: Scheduler = system.scheduler
  implicit val timeout: Timeout = 1 second

  val countResult: Future[Count] = (system ? ((replyTo: ActorRef[Count]) => GetCount(replyTo)))

  countResult.foreach(println)
  Thread.sleep(200)
  system.terminate()
}
