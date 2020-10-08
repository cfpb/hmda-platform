package hmda.publisher.validation

import akka.actor.ActorSystem
import cats.data.{ Validated, ValidatedNel }

import scala.concurrent.{ ExecutionContext, Future }
import cats.syntax.all._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class PublishingGuard(leiCountCheck: LeiCountCheck, tsLinesCheck: TSLinesCheck, messageReporter: MessageReporter)(
  implicit ec: ExecutionContext
) extends LazyLogging {

  def runIfDataIsValid(year: String)(thunk: => Unit): Future[Unit] =
    validate(year).flatMap({
      case Validated.Valid(_) => Future(thunk)
      case Validated.Invalid(errs) =>
        val message = errs.toList.mkString("\n")
        logger.error(s"Data validation failed for year ${year}. Files won't be published. Message:\n${message}")
        messageReporter.report(message)
    })

  private def validate(year: String): Future[ValidatedNel[String, Unit]] =
    for {
      r1 <- leiCountCheck.check(year).map(_.toValidatedNel)
      r2 <- tsLinesCheck.check(year).map(_.toValidatedNel)
    } yield (r1, r2).mapN((_, _) => ())

}

object PublishingGuard {
  def create(dbConfig: DatabaseConfig[JdbcProfile])(implicit as: ActorSystem) = {
    import as.dispatcher
    val config        = ConfigFactory.load("application.conf")
    val leiCountCheck = new LeiCountCheck(dbConfig)
    val tsLinesCheck  = new TSLinesCheck(dbConfig)
    val msgReporter   = new MessageReporter(config.getString("hmda.publisher.validation.reportingUrl"))
    new PublishingGuard(leiCountCheck, tsLinesCheck, msgReporter)
  }
}