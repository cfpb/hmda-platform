package hmda.persistence.processing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.query.{ EventEnvelope, PersistenceQuery }
import akka.persistence.query.scaladsl._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.persistence.messages.CommonMessages._

object HmdaQuery {

  type RJ = ReadJournal with AllPersistenceIdsQuery with CurrentPersistenceIdsQuery with EventsByPersistenceIdQuery with CurrentEventsByPersistenceIdQuery with EventsByTagQuery with CurrentEventsByTagQuery

  case class EventWithSeqNr(seqNr: Long, event: Event)

  val config = ConfigFactory.load()

  val journalId = config.getString("akka.persistence.query.journal.id")

  def readJournal(system: ActorSystem) = {
    PersistenceQuery(system).readJournalFor[RJ](journalId)
  }

  def events(persistenceId: String)(implicit system: ActorSystem, materializer: ActorMaterializer): Source[Event, NotUsed] =
    readJournal(system).currentEventsByPersistenceId(persistenceId, 0L, Long.MaxValue)
      .map {
        case EventEnvelope(_, _, _, event: Event) => event
      }

  def eventsWithSequenceNumber(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long)(implicit system: ActorSystem, materializer: ActorMaterializer): Source[EventWithSeqNr, NotUsed] = {
    readJournal(system)
      .eventsByPersistenceId(persistenceId, fromSequenceNr, toSequenceNr)
      .map {
        case EventEnvelope(_, _, seqNo, event: Event) => EventWithSeqNr(seqNo, event)
      }
  }

}

