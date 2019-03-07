package hmda.calculator.scheduler

import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.actor.HmdaActor
import hmda.calculator.scheduler.schedules.Schedules.APORScheduler

class APORScheduler extends HmdaActor {

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("APORScheduler", self, APORScheduler)
  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("LarScheduler")
  }

  override def receive: Receive = {

    case APORScheduler =>
      println("test")
  }

}
