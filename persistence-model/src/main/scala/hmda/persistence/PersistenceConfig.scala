package hmda.persistence

import com.typesafe.config.ConfigFactory

object PersistenceConfig {
  val isDemo = ConfigFactory.load().getBoolean("hmda.isDemo")

  val configuration = if (isDemo) {
    ConfigFactory.parseResources("application-dev.conf").resolve()
      .withFallback(ConfigFactory.load())
  } else {
    ConfigFactory.load()
  }

}
