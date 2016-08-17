package hmda.persistence.processing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.persistence.CommonMessages._

object HmdaQuery {

  def readJournal(system: ActorSystem) =
    PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  def events(persistenceId: String)(implicit system: ActorSystem, materializer: ActorMaterializer): Source[Event, NotUsed] =
    readJournal(system).currentEventsByPersistenceId(persistenceId)
      .map(_.event.asInstanceOf[Event])

}

