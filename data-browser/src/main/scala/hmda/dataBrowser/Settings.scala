package hmda.dataBrowser

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._

trait Settings {
  val config: Config = ConfigFactory.load()

  private def getDuration(key: String): FiniteDuration = {
    val duration = config.getDuration(key)
    FiniteDuration(duration.toMillis, MILLISECONDS)
  }

  object Server {
    val host: String = config.getString("server.bindings.address")
    val port: Int = config.getInt("server.bindings.port")
    val askTimeout: FiniteDuration = getDuration("server.ask-timeout")
  }

  object Database {
    val tableName: String = config.getString("dbconfig.table")
  }

  object Redis {
    private val host: String = config.getString("redis.hostname")
    private val port: Int = config.getInt("redis.port")
    val url = s"redis://$host:$port"
    val ttl = getDuration("redis.ttl")
  }

}
