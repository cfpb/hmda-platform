package hmda.publisher

import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.actor.{ ActorSystem, Props }
import hmda.publisher.api.HmdaDataPublisherApi
import hmda.publisher.helper.PGTableNameLoader
import hmda.publisher.scheduler._
import hmda.publisher.util.{ MattermostNotifier, PublishingReporter, ScheduleCoordinator }
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

// $COVERAGE-OFF$
object HmdaDataPublisherApp extends App with PGTableNameLoader {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
      | _    _ __  __ _____            _____        _          _____       _     _ _     _
      || |  | |  \/  |  __ \   /\     |  __ \      | |        |  __ \     | |   | (_)   | |
      || |__| | \  / | |  | | /  \    | |  | | __ _| |_ __ _  | |__) |   _| |__ | |_ ___| |__   ___ _ __
      ||  __  | |\/| | |  | |/ /\ \   | |  | |/ _` | __/ _` | |  ___/ | | | '_ \| | / __| '_ \ / _ \ '__|
      || |  | | |  | | |__| / ____ \  | |__| | (_| | || (_| | | |   | |_| | |_) | | \__ \ | | |  __/ |
      ||_|  |_|_|  |_|_____/_/    \_\ |_____/ \__,_|\__\__,_| |_|    \__,_|_.__/|_|_|___/_| |_|\___|_|                                             |___/
    """.stripMargin
  )

  implicit val actorSystem = ActorSystem("hmda-data-publisher")
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  val config               = actorSystem.settings.config

  val mattermostNotifier = new MattermostNotifier(config.getString("hmda.publisher.validation.reportingUrl"))
  val publishingReporter = {
    val groupReportingTimeout = 45.minutes // TODO move to config
    actorSystem.spawn(PublishingReporter(mattermostNotifier, groupReportingTimeout), "PublishingReporter")
  }

  /**
   * Only 1 scheduler should be created
   * QuartzSchedulerExtension in the current version is not thread safe, look into updating dep
   * https://github.com/enragedginger/akka-quartz-scheduler/issues/33
   * */
  private val scheduleCoordinator = actorSystem.spawn(ScheduleCoordinator.behaviors, "ScheduleCoordinator")

  val allSchedulers = AllSchedulers(
    combinedMLarPublicScheduler = actorSystem.actorOf(Props(new CombinedMLarPublicScheduler(publishingReporter, scheduleCoordinator)), "CombinedMLarPublicScheduler"),
    larPublicScheduler = actorSystem.actorOf(Props(new LarPublicScheduler(publishingReporter, scheduleCoordinator)), "LarPublicScheduler"),
    larScheduler = actorSystem.actorOf(Props(new LarScheduler(publishingReporter, scheduleCoordinator)), "LarScheduler"),
    panelScheduler = actorSystem.actorOf(Props(new PanelScheduler(publishingReporter, scheduleCoordinator)), "PanelScheduler"),
    tsPublicScheduler = actorSystem.actorOf(Props(new TsPublicScheduler(publishingReporter, scheduleCoordinator)), "TsPublicScheduler"),
    tsScheduler = actorSystem.actorOf(Props(new TsScheduler(publishingReporter, scheduleCoordinator)), "TsScheduler")
  )

  actorSystem.spawn[Nothing](HmdaDataPublisherApi(allSchedulers), HmdaDataPublisherApi.name)

}
// $COVERAGE-ON$