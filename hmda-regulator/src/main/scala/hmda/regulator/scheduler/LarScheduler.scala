package hmda.regulator.scheduler

import akka.actor.ActorLogging
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.actor.HmdaActor
import hmda.regulator.scheduler.schedules.Schedules.LarScheduler

class LarScheduler extends HmdaActor with ActorLogging {

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("LarScheduler", self, LarScheduler)
  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("LarScheduler")
  }

  override def receive = {
    case x => log.info(s"Received $x")
  }
}
