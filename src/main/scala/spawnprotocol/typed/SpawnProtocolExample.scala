package spawnprotocol.typed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Props, SpawnProtocol}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps
import akka.util.Timeout
import statefulactor.typed.TypedStatefulActor

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

object ActorWithSpawnProtocol {
  def apply(): Behavior[SpawnProtocol.Command] =
    Behaviors.setup { context =>
      // Start initial tasks
      // context.spawn(...)
      SpawnProtocol()
    }
}

object SpawnProtocolExample extends App {

  implicit val system: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(ActorWithSpawnProtocol(), "hello")

  import akka.actor.typed.scaladsl.AskPattern._
  implicit val ec: ExecutionContext = system.executionContext
  implicit val timeout: Timeout = Timeout(3.seconds)


  val spawnedCounter: Future[ActorRef[TypedStatefulActor.Command]] =
    system.ask(SpawnProtocol.Spawn(TypedStatefulActor(), name = "", props = Props.empty, _))

  for (counter <- spawnedCounter) {
    counter ! TypedStatefulActor.Add
  }
  system.terminate()
}
