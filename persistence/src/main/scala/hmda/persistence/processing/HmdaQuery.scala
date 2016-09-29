package hmda.persistence.processing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.scaladsl._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory

object HmdaQuery {

  type RJ = ReadJournal with AllPersistenceIdsQuery with CurrentPersistenceIdsQuery with EventsByPersistenceIdQuery with CurrentEventsByPersistenceIdQuery with EventsByTagQuery with CurrentEventsByTagQuery

  val config = ConfigFactory.load()

  val journalId = config.getString("akka.persistence.query.journal.id")

  def readJournal(system: ActorSystem) = {
    PersistenceQuery(system).readJournalFor[RJ](journalId)
  }

  def allEvents(persistenceId: String)(implicit system: ActorSystem, materializer: ActorMaterializer): Source[Any, NotUsed] =
    readJournal(system)
      .currentEventsByPersistenceId(persistenceId, 0L, Long.MaxValue)
      .map(_.event)

}

