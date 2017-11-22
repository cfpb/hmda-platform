package hmda.publication

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.stream.Supervision.Decider
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.stream.scaladsl.Sink
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.persistence.model.HmdaActor
import hmda.publication.reports.disclosure.DisclosureReports
import hmda.query.repository.filing.FilingCassandraRepository

object HmdaPublication {
  case class GenerateDisclosureByMSAReports(respondentId: String, fipsCode: Int)
  case object PublishRegulatorData
  def props(): Props = Props(new HmdaPublication)

  def createAggregateDisclosureReports(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaPublication.props().withDispatcher("validation-dispatcher"), "hmda-aggregate-disclosure")
  }
}

class HmdaPublication extends HmdaActor with FilingCassandraRepository {

  import HmdaPublication._

  val decider: Decider = { e =>
    repositoryLog.error("Unhandled error in stream", e)
    Supervision.Resume
  }

  override implicit def system = context.system
  val materializerSettings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  override implicit def materializer: ActorMaterializer = ActorMaterializer(materializerSettings)(system)
  override implicit val ec = context.dispatcher

  val fetchSize = config.getInt("hmda.query.fetch.size")

  QuartzSchedulerExtension(system).schedule("Every30Seconds", self, PublishRegulatorData)

  override def receive: Receive = {
    case GenerateDisclosureByMSAReports(respId, fipsCode) =>
      val disclosureReports = new DisclosureReports(system, materializer)
      disclosureReports.generateReports(fipsCode, respId)

    case PublishRegulatorData =>
      log.info(s"Received tick at ${java.time.Instant.now().toEpochMilli}")
      readData(fetchSize)
        .map(lar => lar.toCSV + "\n")
        .runWith(Sink.foreach(println))
  }
}
