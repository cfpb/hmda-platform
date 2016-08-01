package hmda.persistence.processing

import akka.{ Done, NotUsed }
import akka.actor.{ Actor, ActorLogging, Props }
import akka.persistence.query.{ EventEnvelope, PersistenceQuery }
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.CommonMessages._
import hmda.persistence.processing.HmdaRawFile.LineAdded
import hmda.persistence.processing.HmdaRawFileParser.{ ParsingHmdaFileCompleted, StartParsingHmdaFile }

object HmdaRawFileParser {

  val name = "HmdaRawFilePublisher"

  case class StartParsingHmdaFile() extends Command
  case class ParsingHmdaFileCompleted(count: Int, submissionId: String) extends Event

  def props(id: String): Props = Props(new HmdaRawFileParser(id))

}

class HmdaRawFileParser(submissionId: String) extends Actor with ActorLogging {

  var count = 0

  override def receive: Receive = {

    case StartParsingHmdaFile() =>
      streamHmdaRawFile()

    case Shutdown =>
      log.info(s"Parsing completed for $submissionId")
      context stop self

    case _ => // ignore
  }

  def streamHmdaRawFile() = {

    implicit val ec = context.dispatcher

    log.info(s"Parsing HMDA File for submission: $submissionId")

    val system = context.system
    implicit val mat = ActorMaterializer()

    val readJournal = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

    val hmdaRawFileSource: Source[EventEnvelope, NotUsed] =
      readJournal.currentEventsByPersistenceId(s"${HmdaRawFile.name}-$submissionId", 0L, Long.MaxValue)

    val hmdaRawFileEvents: Source[Event, NotUsed] =
      hmdaRawFileSource.map(_.event.asInstanceOf[Event])

    val parsedLars: Source[Either[List[String], LoanApplicationRegister], NotUsed] =
      hmdaRawFileEvents
        .drop(1)
        .map { elem =>
          count += 1
          elem
        }
        .map {
          case l @ LineAdded(_, data, _) =>
            LarCsvParser(data)
        }

    //val sink = Sink.foreach[Event](publisEvent(_))
    val sink = Sink.foreach(println)

    parsedLars
      .runWith(sink)
      .recover { case e => println(e.getLocalizedMessage) }
      .andThen {
        case _ => publisEvent(ParsingHmdaFileCompleted(count, submissionId))
      }
  }

  private def publisEvent(e: Event): Unit = {
    context.system.eventStream.publish(e)
  }

}
