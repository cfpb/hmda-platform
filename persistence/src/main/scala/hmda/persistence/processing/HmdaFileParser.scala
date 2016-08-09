package hmda.persistence.processing

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarCsvParser
import hmda.persistence.CommonMessages._
import hmda.persistence.LocalEventPublisher
import hmda.persistence.processing.HmdaRawFile.LineAdded
import hmda.persistence.processing.HmdaQuery._

object HmdaFileParser {

  val name = "HmdaFileParser"

  case class ReadHmdaRawFile(submissionId: String) extends Command
  case class LarParsed(lar: LoanApplicationRegister) extends Event
  case class LarParsedErrors(errors: List[String]) extends Event

  case class CompleteParsing(submissionId: String) extends Command
  case class ParsingCompleted(submissionId: String) extends Event

  def props(id: String): Props = Props(new HmdaFileParser(id))

  def createHmdaFileParser(system: ActorSystem, submissionId: String): ActorRef = {
    system.actorOf(HmdaFileParser.props(submissionId))
  }

  case class HmdaFileParseState(size: Int = 0, parsingErrors: Seq[List[String]] = Nil) {
    def updated(event: Event): HmdaFileParseState = event match {
      case LarParsed(lar) =>
        HmdaFileParseState(size + 1, parsingErrors)
      case LarParsedErrors(errors) =>
        HmdaFileParseState(size, parsingErrors :+ errors)
    }
  }

}

class HmdaFileParser(submissionId: String) extends PersistentActor with ActorLogging with LocalEventPublisher {

  import HmdaFileParser._

  implicit val system = context.system
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  var state = HmdaFileParseState()

  def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def preStart(): Unit = {
    log.debug(s"Parsing started for $submissionId")
  }

  override def postStop(): Unit = {
    log.debug(s"Parsing ended for $submissionId")
  }

  override def persistenceId: String = s"$name-$submissionId"

  override def receiveCommand: Receive = {

    case ReadHmdaRawFile(persistenceId) =>
      val parsed = events(persistenceId)
        .map { case LineAdded(_, data) => data }
        .drop(1)
        .map(line => LarCsvParser(line))
        .map {
          case Left(errors) => LarParsedErrors(errors)
          case Right(lar) => LarParsed(lar)
        }

      parsed
        .runForeach(parsed => self ! parsed)
        .andThen {
          case _ =>
            self ! Shutdown
        }

    case lp @ LarParsed(lar) =>
      persist(lp) { e =>
        log.debug(s"Persisted: ${e.lar}")
        updateState(e)
      }

    case err @ LarParsedErrors(errors) =>
      persist(err) { e =>
        log.debug(s"Persisted: ${e.errors}")
        updateState(e)
      }

    case GetState =>
      sender() ! state

    case Shutdown =>
      publishEvent(ParsingCompleted(submissionId))
      context stop self

  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

}

