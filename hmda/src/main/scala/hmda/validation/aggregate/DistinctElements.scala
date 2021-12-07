package hmda.validation.aggregate

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{ Keep, Sink, Source }
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.LoanOriginated
import hmda.model.filing.submission.SubmissionId
import hmda.parser.filing.lar.LarCsvParser
import hmda.util.QuarterTimeBarrier
import hmda.util.streams.FlowUtils.framing
import net.openhft.hashing.LongHashFunction

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object DistinctElements extends StrictLogging {
  case class Result(
                     totalCount: Int,
                     distinctCount: Int,
                     uliToDuplicateLineNumbers: Map[String, Vector[Int]],
                     checkType: CheckType,
                     uli: String
                   )

  sealed trait CheckType
  object CheckType {
    case object RawLine        extends CheckType
    case object UniqueLar      extends CheckType
    case object ULI            extends CheckType
    case object ULIActionTaken extends CheckType
    case object ActionTakenWithinRangeCounter extends CheckType
    case object ActionTakenGreaterThanRangeCounter extends CheckType
  }

  private def hashString(s: String): Long = LongHashFunction.xx().hashChars(s)

  def apply(checkType: CheckType, lines: Source[ByteString, NotUsed], submissionId: SubmissionId)(
    implicit materializer: Materializer,
    ec: ExecutionContext
  ): Future[Result] = {
    // checks the state, if the element is already there, it will return false
    // if its not then it will add it and return true
    def checkAndUpdate(state: scala.collection.mutable.Set[Long], incoming: Long): Boolean =
      if (state.contains(incoming)) false
      else {
        state += incoming
        true
      }

    val uploadProgram: Future[Result] =
      lines
        .drop(1) // header
        .via(framing("\n"))
        .map(_.utf8String)
        .map(_.trim)
        .zip(Source.fromIterator(() => Iterator.from(2))) // rows start from #1 but we dropped the header line so we start at #2
        .map {
          case (line, rowNumber) => (LarCsvParser(line), line, rowNumber)
        }
        .collect {
          case (Right(parsed), line, rowNumber) => (parsed, line, rowNumber)
        }
        .statefulMapConcat { () =>
          // state is initialized once when stream is initialized and then reused for the remainder of the stream
          val state: scala.collection.mutable.Set[Long] = scala.collection.mutable.HashSet.empty[Long]

          (each: (LoanApplicationRegister, String, Int)) =>
            each match {
              case (lar: LoanApplicationRegister, rawLine: String, rowNumber: Int) =>
                checkType match {
                  case CheckType.RawLine =>
                    val hashed = hashString(rawLine)
                    List((checkAndUpdate(state, hashed), rowNumber, lar.loan.ULI))

                  case CheckType.UniqueLar =>
                    val hashed = hashString(
                      lar.larIdentifier.LEI + lar.loan.ULI.toUpperCase + lar.action.actionTakenType.code.toString.toUpperCase + lar.action.actionTakenDate.toString.toUpperCase
                    )
                    List((checkAndUpdate(state, hashed), rowNumber, lar.loan.ULI))

                  case CheckType.ULI =>
                    val hashed = hashString(lar.loan.ULI.toUpperCase)
                    List((checkAndUpdate(state, hashed), rowNumber, lar.loan.ULI))

                  case CheckType.ULIActionTaken => // For S306
                    // Only look at ULIs where the actionTakenType.code == 1
                    if (lar.action.actionTakenType == LoanOriginated) {
                      val hashed = hashString(lar.action.actionTakenType.code.toString.toUpperCase + lar.loan.ULI.toUpperCase())
                      List((checkAndUpdate(state, hashed), rowNumber, lar.loan.ULI))
                    } else Nil

                  case CheckType.ActionTakenWithinRangeCounter =>
                    // Only look at LARs with an Action Taken Date within range of the target quarter
                    if (QuarterTimeBarrier.actionTakenInQuarterRange(lar.action.actionTakenDate, submissionId.period)) {
                      val hashed = hashString(lar.action.actionTakenType.code.toString.toUpperCase + lar.action.actionTakenDate.toString + lar.loan.ULI.toUpperCase())
                      List((checkAndUpdate(state, hashed), rowNumber, lar.loan.ULI))
                    } else Nil

                  case CheckType.ActionTakenGreaterThanRangeCounter =>
                    // Only look at LARs with an Action Taken Date greater than the end date of the quarter
                    if (QuarterTimeBarrier.actionTakenGreaterThanRange(lar.action.actionTakenDate, submissionId.period)) {
                      val hashed = hashString(lar.action.actionTakenType.code.toString.toUpperCase + lar.action.actionTakenDate.toString + lar.loan.ULI.toUpperCase())
                      List((checkAndUpdate(state, hashed), rowNumber, lar.loan.ULI))
                    } else Nil
                }
            }

        }
        .toMat(

          Sink.fold(
            Result(totalCount = 0, distinctCount = 0, uliToDuplicateLineNumbers = Map.empty, checkType, submissionId.lei)
          ) {

            // duplicate
            case (acc, (persisted, rowNumber, uliOnWhichErrorTriggered)) if !persisted =>

              acc.copy(
                totalCount = acc.totalCount + 1,
                uliToDuplicateLineNumbers = acc.uliToDuplicateLineNumbers.updated(
                  uliOnWhichErrorTriggered,
                  acc.uliToDuplicateLineNumbers.getOrElse(uliOnWhichErrorTriggered, Vector.empty) :+ rowNumber
                ),
                uli = uliOnWhichErrorTriggered
              ) //the ULI field here is shown as the "id" in /submissions/1/edits/Q600
            // no duplicate
            case (acc, _) => acc.copy(totalCount = acc.totalCount + 1, distinctCount = acc.distinctCount + 1)
          }
        )(Keep.right)
        .named(s"checkForDistinctElements[$checkType]-" + submissionId)
        .run()

    uploadProgram.onComplete {
      case Success(value) =>
        logger.info(s"Check [$checkType] for newsest commit distinct elements has passed for $submissionId")
      case Failure(exception) =>
        logger.error(s"Failed checking [$checkType] for distinct elements $submissionId", exception)
    }

    uploadProgram
  }

}