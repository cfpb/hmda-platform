package hmda.publisher.util

import akka.actor.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.publisher.scheduler.schedules.ScheduleWithYear
import org.slf4j.LoggerFactory

object ScheduleCoordinator {
  private val log = LoggerFactory.getLogger(getClass)

  sealed trait Command
  object Command {
    case class Schedule(name: String, actor: ActorRef, msg: ScheduleWithYear, cron: String) extends Command
    case class Unschedule(name: String) extends Command
  }

  import Command._

  val behaviors: Behavior[Command] = Behaviors.receive {
    (context: ActorContext[Command], command: Command) => {
      val scheduler = QuartzSchedulerExtension(context.system)
      command match {
        case Schedule(name, actor, msg, cron) =>
          try {
            scheduler.createJobSchedule(name, actor, msg, cronExpression = cron)
          } catch {
            case e: Throwable => log.error(s"Unable to schedule $name", e)
          }

        case Unschedule(name) =>
          try {
            scheduler.deleteJobSchedule(name)
          } catch {
            case e: Throwable => log.warn(s"Unable to unschedule $name", e)
          }
      }
    }
    Behaviors.same
  }

  def getLogName: String = {
    log.getName
  }
}
