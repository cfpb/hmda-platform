package hmda.query

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.persistence.query.scaladsl._
import akka.persistence.query.{ EventEnvelope, Offset, PersistenceQuery }
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.messages.CommonMessages.Event
import hmda.messages.submission.HmdaRawDataEvents.LineAdded
import hmda.messages.submission.SubmissionEvents.SubmissionModified
import hmda.model.filing.submission.SubmissionId

object HmdaQuery {

  type RJ =
    ReadJournal
      with PersistenceIdsQuery
      with CurrentPersistenceIdsQuery
      with EventsByPersistenceIdQuery
      with CurrentEventsByPersistenceIdQuery
      with EventsByTagQuery
      with CurrentEventsByTagQuery

  val configuration = ConfigFactory.load()

  val journalId = configuration.getString("akka.persistence.query.journal.id")

  def readJournal(system: ActorSystem[_]): RJ =
    PersistenceQuery(system).readJournalFor[RJ](journalId)

  def eventEnvelopeByTag(tag: String, offset: Offset)(implicit system: ActorSystem[_]): Source[EventEnvelope, NotUsed] =
    readJournal(system).eventsByTag(tag, offset)

  def eventEnvelopeByPersistenceId(persistenceId: String)(implicit system: ActorSystem[_]): Source[EventEnvelope, NotUsed] =
    readJournal(system).eventsByPersistenceId(persistenceId, 0L, Long.MaxValue)

  def currentEventEnvelopeByPersistenceId(persistenceId: String)(implicit system: ActorSystem[_]): Source[EventEnvelope, NotUsed] =
    readJournal(system).currentEventsByPersistenceId(persistenceId, 0L, Long.MaxValue)

  def eventsByPersistenceId(persistenceId: String)(implicit system: ActorSystem[_]): Source[Event, NotUsed] =
    readJournal(system)
      .currentEventsByPersistenceId(persistenceId, 0L, Long.MaxValue)
      .map(e => e.event.asInstanceOf[Event])

  def readRawData(submissionId: SubmissionId)(implicit system: ActorSystem[_]): Source[LineAdded, NotUsed] = {

    val persistenceId = s"HmdaRawData-$submissionId"

    eventsByPersistenceId(persistenceId).collect {
      case evt: LineAdded => evt
    }

  }

  def readSubmission(submissionId: SubmissionId)(implicit system: ActorSystem[_]): Source[SubmissionModified, NotUsed] = {
    val persistenceId = s"Submission-$submissionId"
    eventsByPersistenceId(persistenceId).collect {
      case evt: SubmissionModified => evt
    }
  }

}