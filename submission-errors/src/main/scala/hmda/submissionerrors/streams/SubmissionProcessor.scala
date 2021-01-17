package hmda.submissionerrors.streams

import akka.{ Done, NotUsed }
import akka.actor.typed.ActorSystem
import akka.kafka.CommitterSettings
import akka.kafka.ConsumerMessage.{ CommittableMessage, CommittableOffset }
import akka.kafka.scaladsl.Committer
import akka.stream.scaladsl.{ Flow, Keep, Sink }
import cats.implicits._
import hmda.model.filing.submission.SubmissionId
import hmda.submissionerrors.repositories.{ AddSubmissionError, SubmissionErrorRepository }
import hmda.submissionerrors.streams.ErrorLines.ErrorResult
import hmda.utils.YearUtils
import hmda.utils.YearUtils.Period
import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.Future
// $COVERAGE-OFF$
object SubmissionProcessor {
  sealed trait IncomingData
  object IncomingData {
    final case class Parsed(lei: String, period: Period) extends IncomingData
    final case class Invalid(raw: String, error: String) extends IncomingData
  }

  def processRawKafkaSubmission: Flow[CommittableMessage[String, String], (IncomingData, CommittableOffset), NotUsed] =
    Flow[CommittableMessage[String, String]].map { committableMessage =>
      val leiAndPeriod = committableMessage.record.value()
      val kafkaOffset  = committableMessage.committableOffset
      val parsed = leiAndPeriod.split(":").toList match {
        case lei :: rawPeriod :: Nil =>
          YearUtils.parsePeriod(rawPeriod) match {
            case Left(value)   => IncomingData.Invalid(leiAndPeriod, value.getMessage)
            case Right(period) => IncomingData.Parsed(lei, period)
          }

        case _ =>
          IncomingData.Invalid(leiAndPeriod, "invalid format")
      }
      (parsed, kafkaOffset)
    }

  def handleMessages(
                      repository: SubmissionErrorRepository,
                      kafkaCommitterSettings: CommitterSettings
                    )(implicit system: ActorSystem[_], scheduler: Scheduler): Sink[(IncomingData, CommittableOffset), Future[Done]] =
    Flow[(IncomingData, CommittableOffset)]
      .alsoToMat(handleInvalidMessages(kafkaCommitterSettings))(Keep.right)
      .toMat(handleValidMessages(repository, kafkaCommitterSettings))(Keep.both)
      .mapMaterializedValue {
        case (errorSinkCompletion, normalSinkCompletion) =>
          for {
            _ <- errorSinkCompletion
            _ <- normalSinkCompletion
          } yield Done.done()
      }

  def handleInvalidMessages(kafkaCommitterSettings: CommitterSettings): Sink[(IncomingData, CommittableOffset), Future[Done]] =
    Flow[(IncomingData, CommittableOffset)].collect { case (i: IncomingData.Invalid, o) => (i, o) }
      .log("invalid-messages", {
        case (IncomingData.Invalid(raw, error), _) =>
          s"Failed to parse $raw due to $error"
      })
      .map { case (_, offset) => offset }
      .toMat(Committer.sink(kafkaCommitterSettings))(Keep.right)

  def handleValidMessages(
                           repo: SubmissionErrorRepository,
                           kafkaCommitterSettings: CommitterSettings
                         )(implicit system: ActorSystem[_], monixScheduler: Scheduler): Sink[(IncomingData, CommittableOffset), Future[Done]] =
    Flow[(IncomingData, CommittableOffset)].collect { case (v: IncomingData.Parsed, o) => (v, o) }
      .log("valid-messages", { case (p, _) => s"processing submissions for ${p.lei}:${p.period}" })
      .mapAsync(1) { // process one LEI + Period at a time
        case (IncomingData.Parsed(lei, period), offset) =>
          handleSubmissions(repo, lei, period).as(offset).runToFuture
      }
      .toMat(Committer.sink(kafkaCommitterSettings))(Keep.right)

  /**
   * Each LEI + Period can have many submissions
   * For example:
   *  - (LEI=1, Period=2018, Sequence Number=1)
   *  - (LEI=1, Period=2018, Sequence Number=2)
   *  - (LEI=1, Period=2018, Sequence Number=3)
   *
   * Errors are present per submission
   *
   * @param repo is the repository
   * @param lei is the LEI
   * @param period is the period
   * @param submissionParallelism is the parallelism allowed when processing a LEI + Period pair
   * @param system is the actor system
   * @return
   */
  def handleSubmissions(repo: SubmissionErrorRepository, lei: String, period: Period, submissionParallelism: Int = 2)(
    implicit system: ActorSystem[_]
  ): Task[Unit] =
    for {
      submissions <- Submissions.obtainSubmissions(lei, period)
      _ <- Task.parTraverseN(submissionParallelism)(submissions)(submission =>
        handleSubmission(repo, submission.id, submission.status.code)
      )
    } yield ()

  /**
   * Process a single submission
   * Each submission can have different types of edit errors (i.e. Q609, Q614, Q617)
   * Each line of the submission can have multiple edit errors
   *
   * @param repo is the repository
   * @param submissionId is the LEI + Period + Sequence Number
   * @param system is the actor system
   * @return
   */
  def handleSubmission(repo: SubmissionErrorRepository, submissionId: SubmissionId, status: Int)(
    implicit system: ActorSystem[_]
  ): Task[Unit] =
    repo.submissionPresent(submissionId).flatMap {
      case false =>
        for {
          errorMap           <- ErrorInformation.obtainSubmissionErrors(submissionId)
          enrichedErrorLines <- ErrorLines.obtainLoanData(submissionId)(errorMap)
          dataToAdd = enrichedErrorLines.map {
            case ErrorResult(editName, rowsLoanData) =>
              AddSubmissionError(editName, rowsLoanData.map(_.toString))
          }
          _ <- repo.add(submissionId, status, dataToAdd)
        } yield ()

      case true =>
        Task.unit
    }
}
// $COVERAGE-ON$