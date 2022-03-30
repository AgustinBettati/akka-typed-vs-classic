package statefulactor.classic

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import statefulactor.classic.ClassicStatefulActor.{Add, Count, GetCount}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object ClassicStatefulActor {
  case object Add
  case object GetCount

  case class Count(num: Int)

  def props(): Props = Props(new ClassicStatefulActor)
}


class ClassicStatefulActor extends Actor {
  import ClassicStatefulActor._

  private var counter: Int = 0

  def receive: Receive = {
    case Add => counter += 1
    case GetCount => sender() ! Count(counter)
  }
}

object ClassicStatefulActorRunner extends App {

  val system = ActorSystem("counter")

  val counter: ActorRef = system.actorOf(ClassicStatefulActor.props(), "counter")

  counter ! Add
  counter ! Add
  counter ! Add
  Thread.sleep(200)

  val countResult: Future[Count] = (counter ? GetCount)(1 second).mapTo[Count]

  countResult.foreach(println)
  Thread.sleep(200)
  system.terminate()
}


