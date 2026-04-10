package hmda.persistence.migrator

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.scaladsl.{ CurrentPersistenceIdsQuery, ReadJournal }
import akka.persistence.r2dbc.migration.MigrationTool
import akka.persistence.r2dbc.migration.MigrationTool.Result
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import org.slf4j.LoggerFactory

import scala.util.{ Failure, Success, Try }

object HmdaPersistenceMigrator extends App {
  private val log = LoggerFactory.getLogger(getClass)

  private val main: Behavior[Try[Result]] = Behaviors.setup { context =>
    val migration = new MigrationTool(context.system)

    val migrationConfig = context.system.settings.config.getConfig("akka.persistence.r2dbc.migration")

    val sourceQueryPluginId = migrationConfig.getString("source.query-plugin-id")
    val sourceReadJournal = PersistenceQuery(context.system).readJournalFor[ReadJournal](sourceQueryPluginId)
    val sourcePersistenceIdsQuery = sourceReadJournal.asInstanceOf[CurrentPersistenceIdsQuery]
    val parallelism = migrationConfig.getInt("parallelism")
    implicit val ec = context.executionContext
    implicit val mat = Materializer(context.system)

    sys.env.get("PERSISTENCE_ID") match {
      case Some("DEBUG") =>
        val result = sourcePersistenceIdsQuery
          .currentPersistenceIds()
          .mapAsyncUnordered(parallelism) { persistenceId =>
            log.debug("Migrating {}", persistenceId)
            for {
              x <- migration.migrateEvents(persistenceId)
            } yield persistenceId -> Result(1, x, 0)
          }.map { case (pid, result @ Result(_, events, snapshots)) =>
              log.debug(
                "Migrated persistenceId [{}] with [{}] events{}.",
                pid,
                events,
                if (snapshots == 0) "" else " and snapshot")
              result
          }
          .runWith(Sink.fold(Result.empty) { case (acc, Result(_, events, snapshots)) =>
            val result = Result(acc.persistenceIds + 1, acc.events + events, acc.snapshots + snapshots)
            if (result.persistenceIds % 100 == 0)
              log.info(
                "Migrated [{}] persistenceIds with [{}] events and [{}] snapshots.",
                result.persistenceIds,
                result.events,
                result.snapshots)
            result
          })

        result.transform {
          case s @ Success(Result(persistenceIds, events, snapshots)) =>
            log.info(
              "Migration successful. Migrated [{}] persistenceIds with [{}] events and [{}] snapshots.",
              persistenceIds,
              events,
              snapshots)
            s
          case f @ Failure(exc) =>
            log.error("Migration failed.", exc)
            f
        }
        context.pipeToSelf(result) { result =>
          result
        }
      case Some(pid) =>
        context.pipeToSelf(migration.migrateEvents(pid)) { result =>
          result.map(r => Result(1, r, 0))
        }
      case None =>
        context.pipeToSelf(migration.migrateAll()) { result =>
          result
        }
    }

    Behaviors.receiveMessage {
      case Success(_) =>
        // result already logged by migrateAll
        Behaviors.stopped
      case Failure(_) =>
        Behaviors.stopped
    }
  }

  ActorSystem(main, "HmdaPersistenceMigrator")
}