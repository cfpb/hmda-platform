package hmda.dataBrowser

import com.typesafe.config.{ Config, ConfigFactory }

// $COVERAGE-OFF$
object S3Routes {
  val config: Config = ConfigFactory.load()

  val s3Url: String = config.getString("server.s3.url") + config.getString("server.s3.public-bucket") + "/" + config
    .getString("server.s3.environment") + "/"
  val nationwideCsv: String   = s3Url + config.getString("server.s3.routes.nationwide-csv")
  val nationwidePipe: String  = s3Url + config.getString("server.s3.routes.nationwide-pipe")
  val filteredQueries: String = s3Url + config.getString("server.s3.routes.queries")
}
// $COVERAGE-ON$