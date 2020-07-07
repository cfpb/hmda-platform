package hmda.publisher.helper

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

trait PGTableNameLoader {

  val pgTableConfig    = ConfigFactory.load("application.conf").getConfig("pg-tables")
  val log = LoggerFactory.getLogger("hmda")

  //2018 table names
  val lar2018TableName: String = pgTableConfig.getString("lar2018TableName")
  val mlar2018TableName: String = pgTableConfig.getString("mlar2018TableName")
  val panel2018TableName: String = pgTableConfig.getString("panel2018TableName")
  val ts2018TableName: String = pgTableConfig.getString("ts2018TableName")

  //2019 table names
  val lar2019TableName: String = pgTableConfig.getString("lar2019TableName")
  val mlar2019TableName: String = pgTableConfig.getString("mlar2019TableName")
  val panel2019TableName: String = pgTableConfig.getString("panel2019TableName")
  val ts2019TableName: String = pgTableConfig.getString("ts2019TableName")

  //2020 table names
  val lar2020TableName: String = pgTableConfig.getString("lar2020TableName")
  val mlar2020TableName: String = pgTableConfig.getString("mlar2020TableName")
  val panel2020TableName: String = pgTableConfig.getString("panel2020TableName")
  val ts2020TableName: String = pgTableConfig.getString("ts2020TableName")

  //common table names
  val emailTableName: String = pgTableConfig.getString("emailTableName")

  log.info("Using LAR 2018 Table: "+ lar2018TableName )
  log.info("Using MLAR 2018 Table: "+ mlar2018TableName)
  log.info("Using PANEl 2018 Table: "+ panel2018TableName)
  log.info("Using TS 2018 Table: "+ ts2018TableName)

  log.info("Using LAR 2019 Table: "+ lar2019TableName )
  log.info("Using MLAR 2019 Table: "+ mlar2019TableName)
  log.info("Using PANEl 2019 Table: "+ panel2019TableName)
  log.info("Using TS 2019 Table: "+ ts2019TableName)

  log.info("Using LAR 2020 Table: "+ lar2020TableName )
  log.info("Using MLAR 2020 Table: "+ mlar2020TableName)
  log.info("Using PANEl 2020 Table: "+ panel2020TableName)
  log.info("Using TS 2020 Table: "+ ts2020TableName)

  log.info("Using EMAIL Table: "+ emailTableName)




}
