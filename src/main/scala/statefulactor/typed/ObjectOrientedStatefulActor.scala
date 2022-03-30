package statefulactor.typed

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, DispatcherSelector, Scheduler}
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.util.Timeout
import statefulactor.typed.ObjectOrientedStatefulActor.{Add, Command, Count, GetCount}
import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

object ObjectOrientedStatefulActor {
  sealed trait Command
  case object Add extends Command
  final case class GetCount(replyTo: ActorRef[Count]) extends Command

  final case class Count(n: Int)

  def apply(): Behavior[Command] = {
    Behaviors.setup(context => new ObjectOrientedStatefulActor(context))
  }
}

class ObjectOrientedStatefulActor(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  private var n = 0

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Add =>
        n += 1
        this
      case GetCount(replyTo) =>
        replyTo ! Count(n)
        this
    }
  }
}

object ObjectOrientedStatefulActorRunner extends App {

  val rootBehavior: Behavior[Any] = Behaviors.setup { context =>
    implicit val ec: ExecutionContextExecutor = context.executionContext
    val counter: ActorRef[Command] = context.spawn(ObjectOrientedStatefulActor(), "counter")

    counter ! Add
    counter ! Add
    counter ! Add
    Thread.sleep(200)

    implicit val scheduler: Scheduler = system.scheduler
    implicit val timeout: Timeout = 1 second

    val countResult: Future[Count] = (counter ? (replyTo => GetCount(replyTo)))

    countResult.foreach(println)
    Thread.sleep(200)

    // no behavior for the actor system
    Behaviors.empty
  }

  // setup/teardown
  val system = ActorSystem(rootBehavior, "main-app")

  implicit val ec: ExecutionContext = system.dispatchers.lookup(DispatcherSelector.default)
  system.scheduler.scheduleOnce(1.second, () => system.terminate())

}
