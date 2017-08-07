package hmda.publication.reports

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import hmda.publication.reports.disclosure.DisclosureReports

object AggregateAndDisclosureReports {
  case class GenerateDisclosureByMSAReports(respondentId: String, fipsCode: Int)
  def props(): Props = Props(new AggregateAndDisclosureReports)
  def createAggregateDisclosureReports(system: ActorSystem): ActorRef = {
    system.actorOf(AggregateAndDisclosureReports.props(), "hmda-aggregate-disclosure")
  }
}

class AggregateAndDisclosureReports extends Actor {

  import AggregateAndDisclosureReports._

  implicit val system = context.system
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case GenerateDisclosureByMSAReports(respId, fipsCode) =>
      val disclosureReports = new DisclosureReports(system, materializer)
      disclosureReports.generateReports(fipsCode, respId)
  }
}
