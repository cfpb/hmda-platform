package hmda.publication.reports

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import hmda.publication.reports.disclosure.DisclosureReports
import hmda.query.repository.filing.FilingCassandraRepository

object AggregateAndDisclosureReports {
  case class GenerateDisclosureByMSAReports(respondentId: String, fipsCode: Int)
  def props(): Props = Props(new AggregateAndDisclosureReports)
  def createAggregateDisclosureReports(system: ActorSystem): ActorRef = {
    system.actorOf(AggregateAndDisclosureReports.props())
  }
}

class AggregateAndDisclosureReports extends Actor {

  import AggregateAndDisclosureReports._

  override def receive: Receive = {
    case GenerateDisclosureByMSAReports(respId, fipsCode) =>
      //implicit val system = context.system
      implicit val materializer = ActorMaterializer()
      val disclosureReports = new DisclosureReports(context.system, materializer)
      disclosureReports.generateReports(fipsCode, respId)
  }
}
