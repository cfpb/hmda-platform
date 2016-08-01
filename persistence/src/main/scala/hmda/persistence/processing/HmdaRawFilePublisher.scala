package hmda.persistence.processing

import akka.NotUsed
import akka.actor.{ Actor, ActorLogging, Props }
import akka.persistence.query.{ EventEnvelope, PersistenceQuery }
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.persistence.CommonMessages._
import hmda.persistence.processing.HmdaRawFilePublisher.{ StartStreamingHmdaFile, StreamingHmdaFileCompleted }

object HmdaRawFilePublisher {

  val name = "HmdaRawFilePublisher"

  case class StartStreamingHmdaFile() extends Command
  case class StreamingHmdaFileCompleted(submissionId: String) extends Event

  def props(id: String): Props = Props(new HmdaRawFilePublisher(id))

}

class HmdaRawFilePublisher(submissionId: String) extends Actor with ActorLogging {
  override def receive: Receive = {

    case StartStreamingHmdaFile() =>
      streamHmdaRawFile()

    case Shutdown =>
      context stop self

    case _ => // ignore
  }

  def streamHmdaRawFile() = {

    implicit val ec = context.dispatcher

    log.info(s"Streaming HMDA File for submission: $submissionId")

    val system = context.system
    implicit val mat = ActorMaterializer()

    val readJournal = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

    val hmdaRawFileSource: Source[EventEnvelope, NotUsed] =
      readJournal.currentEventsByPersistenceId(s"${HmdaRawFile.name}-$submissionId", 0L, Long.MaxValue)

    val hmdaRawFileEvents = hmdaRawFileSource.map(_.event)

    val sink = Sink.foreach(println)

    hmdaRawFileEvents
      .runWith(sink)
      .andThen {
        case _ => publisEvent(StreamingHmdaFileCompleted(submissionId))
      }
  }

  private def publisEvent(e: Event): Unit = {
    context.system.eventStream.publish(e)
  }

}
