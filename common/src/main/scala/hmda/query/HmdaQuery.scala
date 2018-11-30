package hmda.query

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.query.{EventEnvelope, Offset, PersistenceQuery}
import akka.persistence.query.scaladsl._
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.messages.CommonMessages.Event

object HmdaQuery {

  type RJ =
    ReadJournal with PersistenceIdsQuery with CurrentPersistenceIdsQuery with EventsByPersistenceIdQuery with CurrentEventsByPersistenceIdQuery with EventsByTagQuery with CurrentEventsByTagQuery

  val configuration = ConfigFactory.load()

  val journalId = configuration.getString("akka.persistence.query.journal.id")

  def readJournal(system: ActorSystem): RJ = {
    PersistenceQuery(system).readJournalFor[RJ](journalId)
  }

  def eventEnvelopeByTag(tag: String, offset: Offset)(
      implicit system: ActorSystem): Source[EventEnvelope, NotUsed] = {
    readJournal(system).eventsByTag(tag, offset)
  }

  def eventEnvelopeByPersistenceId(persistenceId: String)(
      implicit system: ActorSystem): Source[EventEnvelope, NotUsed] = {
    readJournal(system).eventsByPersistenceId(persistenceId, 0L, Long.MaxValue)
  }

  def eventsByPersistenceId(persistenceId: String)(
      implicit system: ActorSystem): Source[Event, NotUsed] = {
    readJournal(system)
      .currentEventsByPersistenceId(persistenceId, 0L, Long.MaxValue)
      .map(e => e.event.asInstanceOf[Event])
  }

}
