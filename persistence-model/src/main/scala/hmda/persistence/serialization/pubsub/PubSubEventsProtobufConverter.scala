package hmda.persistence.serialization.pubsub

import hmda.persistence.messages.commands.publication.PublicationCommands.{ GenerateAggregateReports, GenerateDisclosureReports }
import hmda.persistence.messages.events.pubsub.PubSubEvents.{ FindAggregatePublisher, FindDisclosurePublisher, SubmissionSignedPubSub }
import hmda.persistence.model.serialization.PubSubEvents._
import hmda.persistence.model.serialization.SubmissionEvents.SubmissionIdMessage
import hmda.persistence.serialization.submission.SubmissionProtobufConverter._

object PubSubEventsProtobufConverter {
  def submissionSignedPubSubToProtobuf(obj: SubmissionSignedPubSub): SubmissionSignedPubSubMessage = {
    SubmissionSignedPubSubMessage(
      submissionId = Some(SubmissionIdMessage(obj.submissionId.institutionId, obj.submissionId.period, obj.submissionId.sequenceNumber))
    )
  }
  def submissionSignedPubSubFromProtobuf(msg: SubmissionSignedPubSubMessage): SubmissionSignedPubSub = {
    SubmissionSignedPubSub(
      submissionId = submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }

  def findAggregatePublisherToProtobuf(obj: FindAggregatePublisher): FindAggregatePublisherMessage = {
    FindAggregatePublisherMessage()
  }
  def findAggregatePublisherFromProtobuf(msg: FindAggregatePublisherMessage): FindAggregatePublisher = {
    FindAggregatePublisher()
  }

  def findDisclosurePublisherToProtobuf(obj: FindDisclosurePublisher): FindDisclosurePublisherMessage = {
    FindDisclosurePublisherMessage()
  }
  def findDisclosurePublisherFromProtobuf(msg: FindDisclosurePublisherMessage): FindDisclosurePublisher = {
    FindDisclosurePublisher()
  }

  def generateAggregateReportsToProtobuf(obj: GenerateAggregateReports): GenerateAggregateReportsMessage = {
    GenerateAggregateReportsMessage()
  }
  def generateAggregateReportsFromProtobuf(msg: GenerateAggregateReportsMessage): GenerateAggregateReports = {
    GenerateAggregateReports()
  }

  def generateDisclosureReportsToProtobuf(obj: GenerateDisclosureReports): GenerateDisclosureReportsMessage = {
    GenerateDisclosureReportsMessage(
      submissionId = Some(SubmissionIdMessage(obj.submissionId.institutionId, obj.submissionId.period, obj.submissionId.sequenceNumber))
    )
  }
  def generateDisclosureReportsFromProtobuf(msg: GenerateDisclosureReportsMessage): GenerateDisclosureReports = {
    GenerateDisclosureReports(
      submissionId = submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }

}
