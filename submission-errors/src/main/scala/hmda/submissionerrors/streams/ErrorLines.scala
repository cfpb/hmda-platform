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
// $COVERAGE-OFF$
/**
 * Step 3: Obtain all the lines that had edit errors (quality, syntax, macro, etc.) for each submission that will
 * be inserted into the database
 */
object ErrorLines {
  type RawLine = String
  type ErrorKey = String
  type Fields = Map[String, Map[String, String]]

  final case class RowLoanData(uli: String, actionTaken: Int, actionTakenDate: Int, applicationDate: String) {
    override def toString: String = s"$uli:$actionTaken:$actionTakenDate:$applicationDate"
  }

  final case class ErrorResult(editName: EditName, loanDataRows: Vector[RowLoanData], fields: Fields)

  /**
   * Depends on the results of Step 2 (ErrorInformation.obtainSubmissionErrors)
   * This retrieves the raw data for each submission and then selects all the lines that have failed the validation
   * (which takes place on the command side) that will be persisted into the database for review
   *
   * @param submissionId
   * @param validatedErrors
   * @param system
   * @return a set of ErrorInformation
   */
  def obtainLoanData(
                    submissionId: SubmissionId
                  )(validatedErrors: Set[HmdaRowValidatedError])(implicit system: ActorSystem[_]): Task[Set[ErrorResult]] =
  Task
    .fromFuture(submissionRawData(submissionId).runWith(collectAndParseErrorLines(validatedErrors))).map(_.values.toSet)

  private[streams] def submissionRawData(
                                          submissionId: SubmissionId
                                        )(implicit system: ActorSystem[_]): Source[(RawLine, LineNumber), NotUsed] =
    HmdaQuery
      .currentEventEnvelopeByPersistenceId(s"HmdaRawData-$submissionId")
      .collect {
        case EventEnvelope(_, _, _, event: LineAdded) => event
      }
      .zip(Source.fromIterator(() => Iterator.iterate(1L)(_ + 1L))) // line numbers start at 1
      .drop(1)                                                      // drop header
      .map { case (l, lineNumber) => (l.data, lineNumber) }

  private[streams] def collectAndParseErrorLines(
                                                  validatedErrors: Set[HmdaRowValidatedError]
                                                ): Sink[(RawLine, LineNumber), Future[Map[ErrorKey, ErrorResult]]] =
    Sink.fold[Map[ErrorKey, ErrorResult], (RawLine, LineNumber)](Map.empty) {
      case (acc, (rawData, lineNumber)) =>
        val lar = LarCsvParser(rawData).getOrElse(LoanApplicationRegister())
        val loanData = RowLoanData(lar.loan.ULI, lar.action.actionTakenType.code, lar.action.actionTakenDate, lar.loan.applicationDate)
        validatedErrors.find(_.rowNumber == lineNumber).map(_.validationErrors) match {
          case Some(validationErrors) =>
            validationErrors.foldLeft(acc) { (agg, nextError) =>
              val errorKey = nextError.editName
              val errorResult = agg.getOrElse(errorKey, ErrorResult(nextError.editName, Vector.empty, Map.empty))
              val affectedLoans = errorResult.loanDataRows :+ loanData
              val errorFields = errorResult.fields + (loanData.toString -> nextError.fields)
              agg + (errorKey -> ErrorResult(errorKey, affectedLoans, errorFields))
            }
          case None => acc
        }
    }
}
// $COVERAGE-ON$