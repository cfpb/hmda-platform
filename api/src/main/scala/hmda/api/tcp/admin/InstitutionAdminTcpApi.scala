package hmda.api.tcp.admin

import akka.NotUsed
import akka.pattern.{ ask, pipe }
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Tcp }
import akka.util.{ ByteString, Timeout }
import hmda.api.tcp.TcpApi
import hmda.api.util.FlowUtils
import hmda.model.fi.Filing
import hmda.model.institution.Institution
import hmda.parser.fi.InstitutionParser
import hmda.persistence.HmdaSupervisor.FindFilings
import hmda.persistence.institutions.FilingPersistence.CreateFiling
import hmda.persistence.institutions.{ FilingPersistence, InstitutionPersistence }
import hmda.persistence.institutions.InstitutionPersistence.CreateInstitution
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

object InstitutionAdminTcpApi {
  def props(supervisor: ActorRef): Props = {
    Props(new InstitutionAdminTcpApi(supervisor))
  }
}

class InstitutionAdminTcpApi(supervisor: ActorRef) extends TcpApi with FlowUtils {
  override val name: String = "hmda-institutions-tcp-api"

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher

  override val host: String = config.getString("hmda.panel.tcp.host")
  override val port: Int = config.getInt("hmda.panel.tcp.port")
  val duration = config.getInt("hmda.panel.tcp.timeout").seconds
  implicit val timeout = Timeout(duration)
  val buffer = config.getInt("hmda.panel.tcp.parallelism")

  val tcpHandler: Flow[ByteString, ByteString, NotUsed] =
    Flow[ByteString]
      .via(framing)
      .drop(1)
      .via(byte2StringFlow)
      .map(x => InstitutionParser(x))
      .mapAsync(parallelism = buffer)(i => createInstitution(i))
      .mapAsync(parallelism = buffer)(i => createFiling(i))
      .map(e => ByteString(e.toString))

  override val tcp: Future[Tcp.ServerBinding] = Tcp().bindAndHandle(
    tcpHandler,
    host,
    port
  )

  tcp pipeTo self

  private def createInstitution(i: Institution): Future[Institution] = {
    val fInstitutionsActor = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
    for {
      actor <- fInstitutionsActor
      i <- (actor ? CreateInstitution(i))
        .mapTo[Option[Institution]]
        .map(i => i.getOrElse(Institution.empty))
    } yield i
  }

  private def createFiling(institution: Institution): Future[Filing] = {
    val fFilingPersistence = (supervisor ? FindFilings(FilingPersistence.name, institution.id)).mapTo[ActorRef]
    for {
      actor <- fFilingPersistence
      f <- (actor ? CreateFiling(Filing(institution.activityYear.toString, institution.id)))
        .mapTo[Option[Filing]]
        .map(x => x.getOrElse(Filing()))
    } yield f
  }

}
