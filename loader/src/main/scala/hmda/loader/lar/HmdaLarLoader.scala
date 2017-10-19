package hmda.loader.lar

import java.io.File
import java.time.Instant

import akka.actor.{ ActorPath, ActorRef, ActorSystem }
import akka.pattern.ask
import akka.cluster.client.{ ClusterClient, ClusterClientSettings }
import akka.stream.{ ActorMaterializer, IOResult }
import akka.stream.scaladsl.{ FileIO, Sink, Source }
import akka.util.{ ByteString, Timeout }
import hmda.api.util.FlowUtils
import hmda.model.fi.{ Created, Submission }
import hmda.persistence.HmdaSupervisor.{ FindHmdaFiling, FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.CreateSubmission
import hmda.persistence.processing.HmdaRawFile.AddLine
import hmda.persistence.processing.ProcessingMessages.{ CompleteUpload, Persisted, StartUpload }
import hmda.persistence.processing.SubmissionManager
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.processing.SubmissionManager.AddFileName
import hmda.query.HmdaQuerySupervisor.FindHmdaFilingView
import org.slf4j.LoggerFactory

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object HmdaLarLoader extends FlowUtils {

  override implicit val system = ActorSystem("hmda-cluster-client")
  override implicit val materializer = ActorMaterializer()
  override implicit val ec = system.dispatcher
  val hmdaClusterName = config.getString("hmda.clusterName")
  val hmdaClusterIP = config.getString("hmda.lar.host")
  val hmdaClusterPort = config.getInt("hmda.lar.port")
  val actorTimeout = config.getInt("hmda.actorTimeout")
  val flowParallelism = config.getInt("hmda.lar.parallelism")

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

    val fileName = file.getName
    val parts = fileName.split("_")
    val institutionId = parts.head
    val finalPart = parts.tail.head
    val period = finalPart.substring(0, finalPart.indexOf("."))
    processLars(file, fileName, institutionId, period)

  }

  private def processLars(file: File, fileName: String, institutionId: String, period: String) = {
    val uploadTimestamp = Instant.now.toEpochMilli
    val source = FileIO.fromPath(file.toPath)

    val fSubmissionsActor = (clusterClient ? ClusterClient
      .Send(
        "/user/supervisor/singleton",
        FindSubmissions(SubmissionPersistence.name, institutionId, period),
        localAffinity = true
      )).mapTo[ActorRef]

    (clusterClient ? ClusterClient.Send("/user/supervisor/singleton", FindHmdaFiling(period), localAffinity = true)).mapTo[ActorRef]
    (clusterClient ? ClusterClient.Send("/user/query-supervisor", FindHmdaFilingView(period), localAffinity = true)).mapTo[ActorRef]

    val fUploadSubmission = for {
      s <- fSubmissionsActor
      fSubmission <- (s ? CreateSubmission).mapTo[Option[Submission]]
      submission = fSubmission.getOrElse(Submission())
    } yield (submission, submission.status == Created)

    fUploadSubmission.onComplete {
      case Success((submission, true)) =>
        val message = FindProcessingActor(SubmissionManager.name, submission.id)
        val fProcessingActor = (clusterClient ? ClusterClient.Send("/user/supervisor/singleton", message, localAffinity = true)).mapTo[ActorRef]
        fProcessingActor.onComplete { processingActor =>
          uploadData(processingActor.getOrElse(ActorRef.noSender), uploadTimestamp, fileName, submission, source)
        }

      case Success((_, false)) =>
        log.error("submission not available for upload")
        sys.exit(0)

      case Failure(error) =>
        log.error(error.getLocalizedMessage)
        sys.exit(0)

    }
  }

  private def uploadData(processingActor: ActorRef, uploadTimestamp: Long, fileName: String, submission: Submission, source: Source[ByteString, Future[IOResult]]): Unit = {
    processingActor ! AddFileName(fileName)
    processingActor ! StartUpload
    val uploadedF = source
      .via(framing)
      .map(_.utf8String)
      .mapAsync(parallelism = flowParallelism)(line => (processingActor ? AddLine(uploadTimestamp, line)).mapTo[Persisted.type])
      .runWith(Sink.ignore)

    uploadedF.onComplete {
      case Success(_) =>
        processingActor ! CompleteUpload

      case Failure(error) =>
        processingActor ! Shutdown
        log.error(error.getLocalizedMessage)
    }
  }

}
