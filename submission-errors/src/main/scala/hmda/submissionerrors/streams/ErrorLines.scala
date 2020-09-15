package hmda.submissionerrors.streams

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.persistence.query.EventEnvelope
import akka.stream.scaladsl.{ Sink, Source }
import hmda.messages.submission.HmdaRawDataEvents.LineAdded
import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowValidatedError
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.submission.SubmissionId
import hmda.parser.filing.lar.LarCsvParser
import hmda.query.HmdaQuery
import hmda.submissionerrors.streams.ErrorInformation._
import monix.eval.Task

import scala.concurrent.Future

/**
 * Step 3: Obtain all the lines that had edit errors (quality, syntax, macro, etc.) for each submission that will
 * be inserted into the database
 */
object ErrorLines {
  type RawLine = String

  final case class RowLoanData(uli: String, actionTaken: Int, actionTakenDate: Int) {
    override def toString: String = s"$uli:$actionTaken:$actionTakenDate``"
  }

  def obtainLoanData(
                      submissionId: SubmissionId
                    )(errorMap: Map[LineNumber, Set[EditName]])(implicit system: ActorSystem[_]): Task[List[(SubmissionId, EditName, List[RowLoanData])]] =
    Task
      .fromFuture(submissionRawData(submissionId).runWith(collectAndParseErrorLines(errorMap)))
      .map(enrichedMap => enrichedMap.map { case (k, v) => (submissionId, k, v.toList) }.toList)

  def submissionRawData(submissionId: SubmissionId)(implicit system: ActorSystem[_]): Source[(RawLine, LineNumber), NotUsed] =
    HmdaQuery
      .currentEventEnvelopeByPersistenceId(s"HmdaRawData-$submissionId")
      .collect {
        case EventEnvelope(_, _, _, event: LineAdded) => event
      }
      .zip(Source.fromIterator(() => Iterator.iterate(1L)(_ + 1L))) // line numbers start at 1
      .drop(1)                                                      // drop header
      .map { case (l, lineNumber) => (l.data, lineNumber) }

  def collectAndParseErrorLines(
                                 errorMap: Map[LineNumber, Set[EditName]]
                               ): Sink[(RawLine, LineNumber), Future[Map[EditName, Vector[RowLoanData]]]] =
    Sink.fold[Map[EditName, Vector[RowLoanData]], (RawLine, LineNumber)](Map.empty[EditName, Vector[RowLoanData]]) {
      case (acc, (rawData, lineNumber)) =>
        errorMap.get(lineNumber) match {
          case Some(editNames) =>
            // only parse the line if we found that there was an error associated with it
            val lar      = LarCsvParser(rawData).getOrElse(LoanApplicationRegister())
            val loanData = RowLoanData(lar.loan.ULI, lar.action.actionTakenType.code, lar.action.actionTakenDate)
            editNames.foldLeft(acc) { (acc, nextEditName) =>
              val updatedErrorListForEdit = acc.getOrElse(nextEditName, Vector.empty[RowLoanData]) :+ loanData
              acc + (nextEditName -> updatedErrorListForEdit)
            }
          case None =>
            acc
        }
    }
}