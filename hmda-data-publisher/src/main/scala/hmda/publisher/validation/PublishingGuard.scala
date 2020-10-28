package hmda.publisher.validation

import akka.actor.ActorSystem
import cats.data.{ Validated, ValidatedNel }
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import hmda.publisher.query.component.{ PublisherComponent2018, PublisherComponent2019, PublisherComponent2020 }
import hmda.publisher.validation.PublishingGuard.{ Scope, Year }
import hmda.query.DbConfiguration
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

class PublishingGuard(
                       db2018: PublisherComponent2018,
                       db2019: PublisherComponent2019,
                       db2020: PublisherComponent2020,
                       messageReporter: MessageReporter,
                       dbConfig: DatabaseConfig[JdbcProfile]
                     )(
                       implicit ec: ExecutionContext
                     ) extends LazyLogging {

  def runIfDataIsValid(year: Year, scope: Scope)(thunk: => Unit): Future[Unit] =
    validate(getChecks(year, scope))
      .flatMap({
        case Validated.Valid(_) =>
          logger.debug(s"Data validation successful")
          Future(thunk)
        case Validated.Invalid(errs) =>
          val message = s"Data validation failed for year ${year}. Files won't be published. Message:\n${errs.toList.mkString("\n")}"
          logger.error(message)
          messageReporter.report(message)
      })
      .recoverWith {
        case ex =>
          logger.error(s"Data validation failed with unexpected exception", ex)
          messageReporter.report(s"Data validation failed with unexpected exception: $ex")
      }

  private def getChecks(year: Year, scope: Scope): List[ValidationCheck] = {
    val leiCheckErrorMargin = year match {
      case Year.y2018 => 5
      case Year.y2019 => 0
      case Year.y2020 => 0
    }

    val tsData = year match {
      case Year.y2018 => db2018.validationTSData2018
      case Year.y2019 => db2019.validationTSData2019
      case Year.y2020 => db2020.validationTSData2020
    }

    scope match {
      case Scope.Private =>
        val larData = year match {
          case Year.y2018 => db2018.validationLarData2018
          case Year.y2019 => db2019.validationLarData2019
          case Year.y2020 => db2020.validationLarData2020
        }
        List(
          new TSLinesCheck(dbConfig, tsData, larData),
          new LeiCountCheck(dbConfig, tsData, larData, leiCheckErrorMargin)
        )
      case Scope.Public =>
        // there is no modified lar table for 2020 and so no chcecks will run for this year and scope
        val larDataOpt = year match {
          case Year.y2018 => Some(db2018.validationMLarData2018)
          case Year.y2019 => Some(db2019.validationMLarData2019)
          case Year.y2020 => None
        }
        larDataOpt
          .map(larData =>
            List(
              new TSLinesCheck(dbConfig, tsData, larData),
              new LeiCountCheck(dbConfig, tsData, larData, leiCheckErrorMargin)
            )
          )
          .getOrElse(List())
    }

  }

  private def validate(checks: List[ValidationCheck]): Future[ValidatedNel[String, Unit]] = {
    import cats.instances.future._
    import cats.instances.list._
    import cats.syntax.all._
    val resultsF: Future[List[Either[String, Unit]]]       = checks.traverse(c => c.check())
    val gathered: Future[ValidatedNel[String, List[Unit]]] = resultsF.map(_.traverse(_.toValidatedNel))
    gathered.map(_.map(_ => ()))
  }

}

object PublishingGuard {

  def create(
              dbCompontnents: PublisherComponent2018 with PublisherComponent2019 with PublisherComponent2020
            )(implicit as: ActorSystem): PublishingGuard = {
    import as.dispatcher
    val config      = ConfigFactory.load("application.conf")
    val msgReporter = new MessageReporter(config.getString("hmda.publisher.validation.reportingUrl"))
    val dbConfig    = DbConfiguration.dbConfig
    new PublishingGuard(dbCompontnents, dbCompontnents, dbCompontnents, msgReporter, dbConfig)
  }

  sealed trait Year
  object Year {
    case object y2018 extends Year
    case object y2019 extends Year
    case object y2020 extends Year
  }

  sealed trait Scope
  object Scope {
    case object Public  extends Scope
    case object Private extends Scope
  }
}