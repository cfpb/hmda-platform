package hmda.publication.lar.publication

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import hmda.model.filing.submission.SubmissionId
import hmda.publication.lar.parser.ModifiedLarCsvParser
import hmda.query.HmdaQuery._

sealed trait ModifiedLarCommand
case class UploadToS3(submissionId: SubmissionId) extends ModifiedLarCommand

object ModifiedLarPublisher {

  final val name: String = "ModifiedLarPublisher"

  val behavior: Behavior[ModifiedLarCommand] =
    Behaviors.setup { ctx =>
      implicit val system = ctx.system.toUntyped
      implicit val materializer = ActorMaterializer()
      implicit val ec = system.dispatcher
      val log = ctx.log
      log.info(s"Started $name")

      Behaviors.receiveMessage {

        case UploadToS3(submissionId) =>
          log.info(s"Publishing Modified LAR for $submissionId")
          readRawData(submissionId)
            .map(l => l.data)
            .drop(1)
            .map(s => ModifiedLarCsvParser(s).toCSV + "\n")
            .map { e =>
              println(e); e
            }
            //TODO: push to S3
            .runWith(Sink.ignore)

          Behaviors.same

        case _ =>
          Behaviors.ignore
      }
    }
}
