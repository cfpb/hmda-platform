package hmda.api

import com.typesafe.config.ConfigFactory

object HmdaConfig {

  val isDemo = ConfigFactory.load().getBoolean("hmda.isDemo")

  val configuration = if (isDemo) {
    ConfigFactory.parseResources("application-dev.conf").resolve()
      .withFallback(ConfigFactory.load())
  } else {
    ConfigFactory.load()
  }
}
