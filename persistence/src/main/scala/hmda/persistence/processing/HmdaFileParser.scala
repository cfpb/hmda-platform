package hmda.persistence.processing

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarCsvParser
import hmda.persistence.CommonMessages._
import hmda.persistence.LocalEventPublisher
import hmda.persistence.processing.HmdaRawFile.LineAdded
import hmda.persistence.processing.HmdaQuery._

object HmdaFileParser {

  case class ReadHmdaRawFile(submissionId: String) extends Command
  case class ParsedLar(lar: LoanApplicationRegister) extends Event
  case class ParsedLarErrors(errors: List[String]) extends Event

  case class CompleteParsing(submissionId: String) extends Command
  case class ParsingCompleted(submissionId: String) extends Event

  def props(id: String): Props = Props(new HmdaFileParser(id))

  def createHmdaFileParser(system: ActorSystem, submissionId: String): ActorRef = {
    system.actorOf(HmdaFileParser.props(submissionId))
  }

}

class HmdaFileParser(submissionId: String) extends Actor with ActorLogging with LocalEventPublisher {

  import HmdaFileParser._

  implicit val system = context.system
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def preStart(): Unit = {
    log.info(s"Parsing started for $submissionId")
  }

  override def postStop(): Unit = {
    log.info(s"Parsing ended for $submissionId")
  }

  override def receive: Receive = {

    case ReadHmdaRawFile(persistenceId) =>
      val parsed = events(persistenceId)
        .map { case LineAdded(_, data) => data }
        .drop(1)
        .map(line => LarCsvParser(line))
        .map {
          case Left(errors) => ParsedLarErrors(errors)
          case Right(lar) => ParsedLar(lar)
        }

      parsed
        .runForeach(parsed => self ! parsed)
        .andThen {
          case _ =>
            self ! Shutdown
        }

    case ParsedLar(lar) =>
      log.info(lar.toString)

    case ParsedLarErrors(errors) =>
      log.info(errors.toString())

    case Shutdown =>
      log.info(s"Parsing for $submissionId finished")
      publishEvent(ParsingCompleted(submissionId))
      context stop self

    case _ => //ignore

  }
}

