package hmda.publication.reports.disclosure

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.model.institution.Institution
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.publication.reports.protocol.disclosure.D5XProtocol._
import hmda.query.repository.filing.FilingCassandraRepository
import hmda.query.view.institutions.InstitutionView
import hmda.query.view.institutions.InstitutionView.GetInstitutionByRespondentId

import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json._

class DisclosureReports(val sys: ActorSystem, val mat: ActorMaterializer) extends FilingCassandraRepository {

  override implicit def system: ActorSystem = sys
  override implicit def materializer: ActorMaterializer = mat
  val duration = config.getInt("hmda.actor-lookup-timeout")
  implicit val timeout = Timeout(duration.seconds)

  val larSource = readData(1000)

  def generateReports(fipsCode: Int, respId: String): Future[Unit] = {
    val institutionNameF = institutionName(respId)

    val d51F = D51.generate(larSource, fipsCode, respId, institutionNameF)
    d51F.map { d51 =>
      println(d51.toJson.prettyPrint)
    }

    //val d53F = D53.generate(larSource, fipsCode, respId, institutionNameF)
  }

  private def institutionName(respondentId: String): Future[String] = {
    val querySupervisor = system.actorSelection("/user/query-supervisor")
    val fInstitutionsActor = (querySupervisor ? FindActorByName(InstitutionView.name)).mapTo[ActorRef]
    for {
      a <- fInstitutionsActor
      i <- (a ? GetInstitutionByRespondentId(respondentId)).mapTo[Institution]
    } yield i.respondent.name
  }

}
