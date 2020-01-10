package hmda.persistence

import akka.actor.ActorSystem
import akka.persistence.cassandra.ConfigSessionProvider
import com.datastax.driver.core.QueryLogger
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

class HmdaSessionProvider(system: ActorSystem, config: Config) extends ConfigSessionProvider(system, config) with LazyLogging {

  override def createQueryLogger(): Option[QueryLogger] = QueryLoggerProvider.queryLogger(config)
}