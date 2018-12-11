package hmda.publication.lar

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import hmda.query.HmdaQuery._
import akka.actor.typed.scaladsl.adapter._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import hmda.messages.submission.HmdaRawDataEvents.LineAdded

import scala.util.{Failure, Success}

sealed trait ModifiedLarCommand
case class UploadToS3(submissionId: String) extends ModifiedLarCommand

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
          eventsByPersistenceId(s"HmdaRawData-$submissionId")
            .map(e => e.asInstanceOf[LineAdded])
            .map(l => l.data)
            .drop(1)
            .runWith(Sink.foreach(println))

          Behaviors.same

        case _ =>
          Behaviors.ignore
      }
    }
}
