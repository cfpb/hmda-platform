package hmda.messages.pubsub

import com.typesafe.config.ConfigFactory

object HmdaGroups {

  val config           = ConfigFactory.load()
  val emailGroup       = config.getString("hmda.kafka.groups.emailGroup")
  val modifiedLarGroup = config.getString("hmda.kafka.groups.modifiedLarGroup")
  val irsGroup         = config.getString("hmda.kafka.groups.irsGroup")
  val analyticsGroup   = config.getString("hmda.kafka.groups.analyticsGroup")
  val institutionsGroup = config.getString("hmda.kafka.groups.institutionsGroup")
  val submissionErrorsGroup = config.getString("hmda.kafka.groups.submissionErrorsGroup")
}
