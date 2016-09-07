package hmda.persistence.processing

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.CommonMessages._
import hmda.persistence.{ HmdaPersistentActor, LocalEventPublisher }
import hmda.persistence.processing.HmdaQuery._
import hmda.persistence.processing.HmdaRawFile.LineAdded

object HmdaFileParser {

  val name = "HmdaFileParser"

  case class ReadHmdaRawFile(submissionId: String) extends Command
  case class TsParsed(ts: TransmittalSheet) extends Event
  case class TsParsedErrors(errors: List[String]) extends Event
  case class LarParsed(lar: LoanApplicationRegister) extends Event
  case class LarParsedErrors(errors: List[String]) extends Event

  case class CompleteParsing(submissionId: String) extends Command
  case class ParsingStarted(submissionId: String) extends Event
  case class ParsingCompleted(submissionId: String) extends Event

  def props(id: String): Props = Props(new HmdaFileParser(id))

  def createHmdaFileParser(system: ActorSystem, submissionId: String): ActorRef = {
    system.actorOf(HmdaFileParser.props(submissionId))
  }

  case class HmdaFileParseState(size: Int = 0, parsingErrors: Seq[List[String]] = Nil) {
    def updated(event: Event): HmdaFileParseState = event match {
      case TsParsed(_) | LarParsed(_) =>
        HmdaFileParseState(size + 1, parsingErrors)
      case TsParsedErrors(errors) =>
        HmdaFileParseState(size, parsingErrors :+ errors)
      case LarParsedErrors(errors) =>
        HmdaFileParseState(size, parsingErrors :+ errors)
    }
  }

}

class HmdaFileParser(submissionId: String) extends HmdaPersistentActor with LocalEventPublisher {

  import HmdaFileParser._

  var state = HmdaFileParseState()

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def persistenceId: String = s"$name-$submissionId"

  override def receiveCommand: Receive = {

    case ReadHmdaRawFile(persistenceId) =>
      publishEvent(ParsingStarted(submissionId))
      val parsedTs = events(persistenceId)
        .map { case LineAdded(_, data) => data }
        .take(1)
        .map(line => TsCsvParser(line))
        .map {
          case Left(errors) => TsParsedErrors(errors)
          case Right(ts) => TsParsed(ts)
        }

      parsedTs
        .runForeach(pTs => self ! pTs)

      val parsedLar = events(persistenceId)
        .map { case LineAdded(_, data) => data }
        .drop(1)
        .map(line => LarCsvParser(line))
        .map {
          case Left(errors) => LarParsedErrors(errors)
          case Right(lar) => LarParsed(lar)
        }

      parsedLar
        .runForeach(pLar => self ! pLar)
        .andThen {
          case _ => self ! CompleteParsing
        }

    case tp @ TsParsed(ts) =>
      persist(tp) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case tsErr @ TsParsedErrors(errors) =>
      persist(tsErr) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case lp @ LarParsed(lar) =>
      persist(lp) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case larErr @ LarParsedErrors(errors) =>
      persist(larErr) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case CompleteParsing =>
      log.debug(s"Parsing completed for $submissionId")
      publishEvent(ParsingCompleted(submissionId))

    case GetState =>
      sender() ! state

    case Shutdown =>
      context stop self

  }

}

