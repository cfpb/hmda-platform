package hmda.persistence.processing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.scaladsl._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.persistence.PersistenceConfig._
import hmda.persistence.messages.CommonMessages._

object HmdaQuery {

  type RJ = ReadJournal with AllPersistenceIdsQuery with CurrentPersistenceIdsQuery with EventsByPersistenceIdQuery with CurrentEventsByPersistenceIdQuery with EventsByTagQuery2 with CurrentEventsByTagQuery2

  case class EventWithSeqNr(seqNr: Long, event: Event)

  val journalId = configuration.getString("akka.persistence.query.journal.id")

  def readJournal(system: ActorSystem) = {
    PersistenceQuery(system).readJournalFor[RJ](journalId)
  }

  def events(persistenceId: String)(implicit system: ActorSystem, materializer: ActorMaterializer): Source[Event, NotUsed] = {
    readJournal(system).currentEventsByPersistenceId(persistenceId, 0L, Long.MaxValue)
      .map(_.event.asInstanceOf[Event])
  }

  def eventsWithSequenceNumber(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long)(implicit system: ActorSystem, materializer: ActorMaterializer): Source[EventWithSeqNr, NotUsed] = {
    readJournal(system)
      .eventsByPersistenceId(persistenceId, fromSequenceNr, toSequenceNr)
      .map(x => EventWithSeqNr(x.sequenceNr, x.event.asInstanceOf[Event]))
  }

}

