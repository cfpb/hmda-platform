package hmda.persistence.migrator

import akka.actor.typed.ActorSystem
import akka.persistence.r2dbc.migration.MigrationTool.Result

import scala.util.Try

object HmdaPersistenceApp extends App {
  sys.env.get("MODE") match {
    case Some("server") => ActorSystem[Nothing](HmdaPersistenceAdhocMigrationServer.main, HmdaPersistenceAdhocMigrationServer.name)
    case Some("reconcile") => ActorSystem[Any](HmdaPersistenceReconciler.main, HmdaPersistenceReconciler.name)
    case _ => ActorSystem[Try[Result]](HmdaPersistenceMigrator.main, HmdaPersistenceMigrator.name)
  }
}
