package behaviorfactory.typed

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.StashBuffer
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import behaviorfactory.typed.ComplexActor.Command

object ComplexActor {

  // behavior factory method
  def apply(name: String): Behavior[Command] = {
    Behaviors.setup { context =>
      // create child actors..
      Behaviors.withStash(20) { stashBuffer =>
        Behaviors.withTimers { timers =>
          ComplexActor(name, timers, stashBuffer).running(someState = 0)
        }
      }
    }
  }

  sealed trait Command
}

case class ComplexActor(name: String, timers: TimerScheduler[Command], stash: StashBuffer[Command]){

  def running(someState: Int): Behavior[Command] = Behaviors.receiveMessage(_ => ???)
}
