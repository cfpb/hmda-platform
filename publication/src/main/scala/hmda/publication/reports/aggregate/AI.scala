package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.ActorRef
import akka.cluster.singleton.{ ClusterSingletonProxy, ClusterSingletonProxySettings }
import akka.pattern.ask
import akka.stream.scaladsl._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.persistence.HmdaSupervisor
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.publication.reports.{ AS, EC, MAT }

import scala.concurrent.duration._
import scala.concurrent.Future

object AI extends AggregateReport {
  val reportId: String = "AI"
  def filters(lar: LoanApplicationRegister): Boolean = true

  val configuration = ConfigFactory.load()
  val duration = configuration.getInt("hmda.actor.timeout")
  implicit val timeout = Timeout(duration.seconds)

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int
  ): Future[AggregateReportPayload] = {
    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars = larSource
      .filter(lar => lar.geography.msa != "NA")
      .filter(lar => lar.geography.msa.toInt == fipsCode)

    val msa: String = s""""msa": ${msaReport(fipsCode.toString).toJsonFormat},"""
    val reportDate = formattedCurrentDate
    val yearF = calculateYear(lars)

    for {
      year <- yearF
      i <- getInstitutions
      names <- larsToInst(lars, i)
    } yield {
      val namesStr = names.mkString("[", ",", "]")
      val report = s"""
                      |{
                      |    "table": "${metaData.reportTable}",
                      |    "type": "${metaData.reportType}",
                      |    "description": "${metaData.description}",
                      |    "year": "$year",
                      |    "reportDate": "$reportDate",
                      |    $msa
                      |    "institutions": $namesStr
                      |}
       """.stripMargin

      AggregateReportPayload(reportId, fipsCode.toString, report)
    }
  }

  private def larsToInst[ec: EC, mat: MAT, as: AS](lars: Source[LoanApplicationRegister, NotUsed], institutions: Set[Institution]): Future[Set[String]] = {
    val allRespondentIdsF = lars.map(_.respondentId).runWith(Sink.seq)
    allRespondentIdsF.map { ids =>
      institutions.filter(i => ids.contains(i.respondentId)).map(i => s""""${i.respondent.name}"""")
    }
  }

  private def getInstitutions(implicit system: AS[_], ec: EC[_]): Future[Set[Institution]] = {
    for {
      a <- institutionPersistence
      i <- (a ? GetState).mapTo[Set[Institution]]
    } yield i
  }

  def institutionPersistence(implicit system: AS[_], ec: EC[_]): Future[ActorRef] = {
    val supervisorF: Future[ActorRef] = if (configuration.getBoolean("hmda.isClustered")) {
      Future(system.actorOf(
        ClusterSingletonProxy.props(
          singletonManagerPath = s"/user/${HmdaSupervisor.name}",
          settings = ClusterSingletonProxySettings(system).withRole("persistence")
        )
      ))

    } else {
      system.actorSelection(s"/user/${HmdaSupervisor.name}").resolveOne()
    }

    supervisorF.flatMap(supervisor => (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef])
  }

}
