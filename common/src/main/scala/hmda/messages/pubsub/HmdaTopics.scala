package hmda.messages.pubsub

import com.typesafe.config.ConfigFactory

object HmdaTopics {

  val config = ConfigFactory.load()

  val institutionTopic = config.getString("hmda.kafka.topics.institutionTopic")
  val signTopic        = config.getString("hmda.kafka.topics.signTopic")
  val modifiedLarTopic = config.getString("hmda.kafka.topics.modifiedLarTopic")
  val irsTopic         = config.getString("hmda.kafka.topics.irsTopic")
  val analyticsTopic   = config.getString("hmda.kafka.topics.analyticsTopic")
  val disclosureTopic  = config.getString("hmda.kafka.topics.disclosureTopic")
  val adTopic          = config.getString("hmda.kafka.topics.adTopic")
  val emailTopic       = config.getString("hmda.kafka.topics.emailTopic")
}
