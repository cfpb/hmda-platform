package hmda.regulator.scheduler

import akka.actor.ActorLogging
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.actor.HmdaActor
import hmda.regulator.scheduler.schedules.Schedules.TsScheduler

class TsScheduler extends HmdaActor with ActorLogging {

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("TsScheduler", self, TsScheduler)
  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler")
  }

  override def receive = {
    case x => log.info(s"Received $x")
  }
}
