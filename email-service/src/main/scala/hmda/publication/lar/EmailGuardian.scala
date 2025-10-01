package hmda.publication.lar

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl._
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem => UntypedActorSystem}
import akka.kafka.CommitterSettings
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.Keep
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import hmda.publication.lar.config.Settings
import hmda.publication.lar.database.{EmailSubmissionStatusRepository, PGSlickEmailSubmissionStatusRepository}
import hmda.publication.lar.email.SESEmailService
import hmda.publication.lar.streams.SubmissionStream
import hmda.publication.lar.streams.AdminStream
import monix.execution.Scheduler
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object EmailGuardian {
  sealed trait GuardianProtocol
  case object Ready                 extends GuardianProtocol
  case class Error(message: String) extends GuardianProtocol

  def apply(): Behavior[GuardianProtocol] =
    Behaviors.setup { ctx =>
      implicit val system: UntypedActorSystem = ctx.system.toClassic
      implicit val mat: Materializer          = Materializer(ctx)
      implicit val monixScheduler: Scheduler  = Scheduler(ctx.executionContext)

      val databaseConfig                                   = DatabaseConfig.forConfig[JdbcProfile]("db")
      val config                                           = Settings(system)
      val serviceClient                                    = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).build()
      val emailService                                     = new SESEmailService(serviceClient, config.email.fromAddress)
      val emailStatusRepo: EmailSubmissionStatusRepository = new PGSlickEmailSubmissionStatusRepository(databaseConfig)
      val commitSettings                                   = CommitterSettings(config.kafka.commitSettings)

      val (submissionControl, streamSubmissionCompletion) =
        SubmissionStream.pullEmails(system, config.kafka.bootstrapServers)
          .via(
            SubmissionStream.sendEmailsIfNecessary(
              emailService,
              emailStatusRepo,
              config.email.content,
              config.email.subject,
              config.email.parallelism,
              config.email.timeToRetry,
              config.bankFilterList
            )
          )
          .asSource
          .map { case (_, offset) => offset }
          .toMat(SubmissionStream.commitMessages(commitSettings))(Keep.both)
          .run()

      val (adminControl, streamAdminCompletion) =
        AdminStream.pullEmails(system, config.kafka.bootstrapServers)
          .via(
            AdminStream.sendEmailsIfNecessary(
              emailService,
              config.email.parallelism,
              config.email.timeToRetry
            )
          )
          .asSource
          .map { case (_, offset) => offset }
          .toMat(AdminStream.commitMessages(commitSettings))(Keep.both)
          .run()

      streamSubmissionCompletion.onComplete { t =>
        val errorMessage = t.fold(e => e.getMessage, _ => "infinite stream completed")
        ctx.self ! Error(errorMessage)
      }

      streamAdminCompletion.onComplete { t =>
        val errorMessage = t.fold(e => e.getMessage, _ => "infinite stream completed")
        ctx.self ! Error(errorMessage)
      }


      Behaviors.receiveMessage {
        case Error(errorMessage) =>
          ctx.log.error(s"Infinite consumer stream has terminated, Error: $errorMessage")
          submissionControl.drainAndShutdown(streamSubmissionCompletion).onComplete { _ =>
            serviceClient.shutdown()
            databaseConfig.db.shutdown
          }
          adminControl.drainAndShutdown(streamAdminCompletion).onComplete { _ =>
            serviceClient.shutdown()
            databaseConfig.db.shutdown
          }
          Behaviors.stopped
      }
    }
}