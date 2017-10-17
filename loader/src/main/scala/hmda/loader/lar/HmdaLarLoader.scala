package hmda.loader.lar

import java.io.File

import akka.actor.{ActorPath, ActorRef, ActorSystem}
import akka.pattern.ask
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.util.FlowUtils
import hmda.model.fi.{Created, Submission, SubmissionId}
import hmda.persistence.HmdaSupervisor.{FindProcessingActor, FindSubmissions}
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.processing.SubmissionManager
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object HmdaLarLoader extends FlowUtils {

  override implicit val system = ActorSystem("hmda-cluster-client")
  override implicit val materializer = ActorMaterializer()
  override implicit val ec = system.dispatcher
  val hmdaClusterName = config.getString("hmda.clusterName")
  val hmdaClusterIP = config.getString("hmda.lar.host")
  val hmdaClusterPort = config.getInt("hmda.lar.port")
  val actorTimeout = config.getInt("hmda.actorTimeout")

  implicit val timeout = Timeout(actorTimeout.seconds)

  val log = LoggerFactory.getLogger("hmda-lar-loader")

  val initialContacts = Set(
    ActorPath.fromString(s"akka.tcp://$hmdaClusterName@$hmdaClusterIP:$hmdaClusterPort/system/receptionist")
  )

  val settings = ClusterClientSettings(system)
    .withInitialContacts(initialContacts)

  val clusterClient = system.actorOf(ClusterClient.props(settings), "hmda-lar-loader")

  def main(args: Array[String]): Unit = {

    if (args.length < 1) {
      exitSys(log, "No File argument provided", 1)
    }

    val file = new File(args(0))
    if (!file.exists() || !file.isFile) {
      exitSys(log, "File does not exist", 2)
    }

    val source = FileIO.fromPath(file.toPath)

    source.take(1)
        .runWith(Sink.foreach(println))





    val institutionId = "institutionID"
    val period = "2017"

    //processLars(institutionId, period)

  }

  private def processLars(institutionId: String, period: String) = {
    val submissionId = SubmissionId(institutionId, period)

    val message = FindProcessingActor(SubmissionManager.name, submissionId)

    val fProcessingActor = (clusterClient ? ClusterClient.Send("/user/supervisor/singleton", message, localAffinity = true)).mapTo[ActorRef]
    val fSubmissionsActor = (clusterClient ? ClusterClient
      .Send("/user/supervisor/singleton",
        FindSubmissions(SubmissionPersistence.name, institutionId, period),
        localAffinity = true)).mapTo[ActorRef]

    //TODO: Do we need this to load previous year's data?
    //(clusterClient ? ClusterClient.Send("/user/supervisor/singleton", FindHmdaFiling(period), localAffinity = true)).mapTo[ActorRef]
    //(clusterClient ? ClusterClient.Send("/user/query-supervisor", FindHmdaFilingView(period), localAffinity = true)).mapTo[ActorRef]

    val fUploadSubmission = for {
      p <- fProcessingActor
      s <- fSubmissionsActor
      fSubmission <- (s ? GetSubmissionById(submissionId)).mapTo[Submission]
    } yield (fSubmission, fSubmission.status == Created, p)

    fUploadSubmission.onComplete {
      case Success((submission, true, processingActor)) =>
        uploadData(processingActor, 0L, submission)

      case Success((_, false, _)) =>
        log.error("submission not available for upload")
        sys.exit(0)

      case Failure(error) =>
        log.error(error.getLocalizedMessage)
        sys.exit(0)

    }
  }

  private def uploadData(processingActor: ActorRef, uploadTimestamp: Long, submission:Submission ): Unit = ???

}
