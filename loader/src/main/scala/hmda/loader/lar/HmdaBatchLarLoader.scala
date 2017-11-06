package hmda.loader.lar

import java.io.File
import java.time.Instant

import akka.pattern.ask
import akka.actor.{ ActorPath, ActorRef, ActorSystem }
import akka.cluster.client.{ ClusterClient, ClusterClientSettings }
import akka.stream.{ ActorMaterializer, IOResult }
import akka.stream.scaladsl.{ FileIO, Sink, Source }
import akka.util.{ ByteString, Timeout }
import hmda.api.util.FlowUtils
import hmda.model.fi.{ Created, Filing, Submission }
import hmda.persistence.HmdaSupervisor.{ FindFilings, FindHmdaFiling, FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.FilingPersistence.CreateFiling
import hmda.persistence.institutions.SubmissionPersistence.CreateSubmission
import hmda.persistence.institutions.{ FilingPersistence, SubmissionPersistence }
import hmda.persistence.processing.HmdaRawFile.AddLine
import hmda.persistence.processing.ProcessingMessages.{ CompleteUpload, Persisted, StartUpload }
import hmda.persistence.processing.SubmissionManager
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.processing.SubmissionManager.AddFileName
import hmda.query.HmdaQuerySupervisor.FindHmdaFilingView
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object HmdaBatchLarLoader extends FlowUtils {

  override implicit val system: ActorSystem = ActorSystem("hmda-cluster-client")
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = system.dispatcher

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

    if (args.length < 2) {
      exitSys(log, "Please provide a directory containing files with LAR data and period to process", 1)
    }

    val path = new File(args(0))
    if (!path.isDirectory) {
      exitSys(log, "Argument must be the full path to a directory containing files with LAR data", 2)
    }

    val period = args(1)

    val fileList = path.listFiles().toSet
    val fileNames = fileList.map(file => file.getName).filter(name => name.endsWith(".txt"))

    fileNames.foreach(fileName => processLars(new File(s"$path/$fileName"), s"$fileName", s"${fileName.substring(0, fileName.indexOf("."))}", period))

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
    //TODO: turn back on when #1282 is done
    //(clusterClient ? ClusterClient.Send("/user/query-supervisor", FindHmdaFilingView(period), localAffinity = true)).mapTo[ActorRef]

    val fUploadSubmission = for {
      s <- fSubmissionsActor
      fSubmission <- (s ? CreateSubmission).mapTo[Option[Submission]]
      submission = fSubmission.getOrElse(Submission())
    } yield (submission, submission.status == Created)

    fUploadSubmission.onComplete {
      case Success((submission, true)) =>
        val findSubmissionManager = FindProcessingActor(SubmissionManager.name, submission.id)
        val findFilingPersistence = FindFilings(FilingPersistence.name, institutionId)
        val fProcessingActor = (clusterClient ? ClusterClient.Send("/user/supervisor/singleton", findSubmissionManager, localAffinity = true)).mapTo[ActorRef]
        val fFilingPersistence = (clusterClient ? ClusterClient.Send("/user/supervisor/singleton", findFilingPersistence, localAffinity = true)).mapTo[ActorRef]

        for {
          f <- fFilingPersistence
          p <- fProcessingActor
          _ <- (f ? CreateFiling(Filing(period, institutionId))).mapTo[Option[Filing]]
        } yield {
          uploadData(p, uploadTimestamp, fileName, submission, source)
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
