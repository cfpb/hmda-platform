package hmda.dataBrowser

import java.math.BigInteger
import java.security.MessageDigest

import com.typesafe.config.{ Config, ConfigFactory }
import hmda.dataBrowser.models.ModifiedLarTable

import scala.concurrent.duration._
// $COVERAGE-OFF$
trait Settings {
  private val config: Config = ConfigFactory.load()

  private def getDuration(key: String): FiniteDuration = {
    val duration = config.getDuration(key)
    FiniteDuration(duration.toMillis, MILLISECONDS)
  }

  def md5HashString(s: String): String = {
    val md           = MessageDigest.getInstance("MD5")
    val digest       = md.digest(s.getBytes)
    val bigInt       = new BigInteger(1, digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }
  object server {
    val host: String               = config.getString("server.bindings.address")
    val port: Int                  = config.getInt("server.bindings.port")
    val askTimeout: FiniteDuration = getDuration("server.ask-timeout")
  }

  object database {
    val tableName2019: String = config.getString("dbconfig.table.2019")
    val tableName2018: String = config.getString("dbconfig.table.2018")
    val tableName2017: String = config.getString("dbconfig.table.2017")

    // note that 2017 is a special case as the data does not have the same format as 2018+
    def tableSelector(year: Int): ModifiedLarTable = {
      val selected = year match {
        case 2018 => tableName2018
        case 2019 => tableName2019
        case _    => tableName2019
      }
      ModifiedLarTable(selected)
    }
  }

  object redis {
    private val host: String = config.getString("redis.hostname")
    private val port: Int    = config.getInt("redis.port")
    val url                  = s"redis://$host:$port"
    val ttl: FiniteDuration  = getDuration("redis.ttl")
  }

  object s3 {
    val environment: String = config.getString("server.s3.environment")
    val bucket: String      = config.getString("server.s3.public-bucket")
    val url: String         = config.getString("server.s3.url")
    def tableSelector(year: Int): String = {
      val selected = year match {
        case 2017 => config.getString("server.s3.routes.filtered-queries.2017")
        case 2018 => config.getString("server.s3.routes.filtered-queries.2018")
        case 2019 => config.getString("server.s3.routes.filtered-queries.2019")
        case _ => config.getString("server.s3.routes.filtered-queries.2019")
      }
      selected
    }
  }
}
// $COVERAGE-OFF$