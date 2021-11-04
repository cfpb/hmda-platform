package hmda.publisher.helper

import com.typesafe.config.ConfigFactory

trait PGTableNameLoader {

  val pgTableConfig = ConfigFactory.load("application.conf").getConfig("pg-tables")

  //2018 table names
  val lar2018TableName: String   = pgTableConfig.getString("lar2018TableName")
  val mlar2018TableName: String  = pgTableConfig.getString("mlar2018TableName")
  val panel2018TableName: String = pgTableConfig.getString("panel2018TableName")
  val ts2018TableName: String    = pgTableConfig.getString("ts2018TableName")

  val lar2018QATableName: String       = pgTableConfig.getString("lar2018QATableName")
  val mlar2018QATableName: String      = pgTableConfig.getString("mlar2018QATableName")
  val panel2018QATableName: String     = pgTableConfig.getString("panel2018QATableName")
  val ts2018PublicQATableName: String  = pgTableConfig.getString("ts2018PublicQATableName")
  val ts2018PrivateQATableName: String = pgTableConfig.getString("ts2018PrivateQATableName")

  //2019 table names
  val lar2019TableName: String   = pgTableConfig.getString("lar2019TableName")
  val mlar2019TableName: String  = pgTableConfig.getString("mlar2019TableName")
  val panel2019TableName: String = pgTableConfig.getString("panel2019TableName")
  val ts2019TableName: String    = pgTableConfig.getString("ts2019TableName")

  val lar2019QATableName: String          = pgTableConfig.getString("lar2019QATableName")
  val lar2019QALoanLimitTableName: String = pgTableConfig.getString("lar2019QALoanLimitTableName")
  val mlar2019QATableName: String         = pgTableConfig.getString("mlar2019QATableName")
  val panel2019QATableName: String        = pgTableConfig.getString("panel2019QATableName")
  val ts2019PublicQATableName: String     = pgTableConfig.getString("ts2019PublicQATableName")
  val ts2019PrivateQATableName: String    = pgTableConfig.getString("ts2019PrivateQATableName")

  //2020 table names
  val mlar2020TableName: String   = pgTableConfig.getString("mlar2020TableName")
  val mlar2020QATableName: String = pgTableConfig.getString("mlar2020QATableName")

  val panel2020TableName: String   = pgTableConfig.getString("panel2020TableName")
  val panel2020QATableName: String = pgTableConfig.getString("panel2020QATableName")

  val ts2020TableName: String   = pgTableConfig.getString("ts2020TableName")
  val ts2020Q1TableName: String = pgTableConfig.getString("ts2020Q1TableName")
  val ts2020Q2TableName: String = pgTableConfig.getString("ts2020Q2TableName")
  val ts2020Q3TableName: String = pgTableConfig.getString("ts2020Q3TableName")

  val ts2020QATableName: String   = pgTableConfig.getString("ts2020QATableName")
  val ts2020Q1QATableName: String = pgTableConfig.getString("ts2020Q1QATableName")
  val ts2020Q2QATableName: String = pgTableConfig.getString("ts2020Q2QATableName")
  val ts2020Q3QATableName: String = pgTableConfig.getString("ts2020Q3QATableName")

  val lar2020TableName: String   = pgTableConfig.getString("lar2020TableName")
  val lar2020QALoanLimitTableName: String = pgTableConfig.getString("lar2020QALoanLimitTableName")
  val lar2020Q1TableName: String = pgTableConfig.getString("lar2020Q1TableName")
  val lar2020Q2TableName: String = pgTableConfig.getString("lar2020Q2TableName")
  val lar2020Q3TableName: String = pgTableConfig.getString("lar2020Q3TableName")

  val lar2020QATableName: String   = pgTableConfig.getString("lar2020QATableName")
  val lar2020Q1QATableName: String = pgTableConfig.getString("lar2020Q1QATableName")
  val lar2020Q2QATableName: String = pgTableConfig.getString("lar2020Q2QATableName")
  val lar2020Q3QATableName: String = pgTableConfig.getString("lar2020Q3QATableName")

  //2021 table names
  val mlar2021TableName: String   = pgTableConfig.getString("mlar2021TableName")
  val mlar2021QATableName: String = pgTableConfig.getString("mlar2021QATableName")

  val panel2021TableName: String   = pgTableConfig.getString("panel2021TableName")
  val panel2021QATableName: String = pgTableConfig.getString("panel2021QATableName")

  val ts2021TableName: String   = pgTableConfig.getString("ts2021TableName")
  val ts2021Q1TableName: String = pgTableConfig.getString("ts2021Q1TableName")
  val ts2021Q2TableName: String = pgTableConfig.getString("ts2021Q2TableName")
  val ts2021Q3TableName: String = pgTableConfig.getString("ts2021Q3TableName")

  val ts2021QATableName: String   = pgTableConfig.getString("ts2021QATableName")
  val ts2021Q1QATableName: String = pgTableConfig.getString("ts2021Q1QATableName")
  val ts2021Q2QATableName: String = pgTableConfig.getString("ts2021Q2QATableName")
  val ts2021Q3QATableName: String = pgTableConfig.getString("ts2021Q3QATableName")

  val lar2021TableName: String   = pgTableConfig.getString("lar2021TableName")
  val lar2021QALoanLimitTableName: String = pgTableConfig.getString("lar2021QALoanLimitTableName")
  val lar2021Q1TableName: String = pgTableConfig.getString("lar2021Q1TableName")
  val lar2021Q2TableName: String = pgTableConfig.getString("lar2021Q2TableName")
  val lar2021Q3TableName: String = pgTableConfig.getString("lar2021Q3TableName")

  val lar2021QATableName: String   = pgTableConfig.getString("lar2021QATableName")
  val lar2021Q1QATableName: String = pgTableConfig.getString("lar2021Q1QATableName")
  val lar2021Q2QATableName: String = pgTableConfig.getString("lar2021Q2QATableName")
  val lar2021Q3QATableName: String = pgTableConfig.getString("lar2021Q3QATableName")


  val lar2022TableName: String   = pgTableConfig.getString("lar2022TableName")
  val lar2022QALoanLimitTableName: String = pgTableConfig.getString("lar2022QALoanLimitTableName")
  val lar2022Q1TableName: String = pgTableConfig.getString("lar2022Q1TableName")
  val lar2022Q2TableName: String = pgTableConfig.getString("lar2022Q2TableName")
  val lar2022Q3TableName: String = pgTableConfig.getString("lar2022Q3TableName")

  val lar2022QATableName: String   = pgTableConfig.getString("lar2022QATableName")
  val lar2022Q1QATableName: String = pgTableConfig.getString("lar2022Q1QATableName")
  val lar2022Q2QATableName: String = pgTableConfig.getString("lar2022Q2QATableName")
  val lar2022Q3QATableName: String = pgTableConfig.getString("lar2022Q3QATableName")


  val panel2022TableName: String   = pgTableConfig.getString("panel2022TableName")
  val panel2022QATableName: String = pgTableConfig.getString("panel2022QATableName")

  val ts2022TableName: String   = pgTableConfig.getString("ts2022TableName")
  val ts2022Q1TableName: String = pgTableConfig.getString("ts2022Q1TableName")
  val ts2022Q2TableName: String = pgTableConfig.getString("ts2022Q2TableName")
  val ts2022Q3TableName: String = pgTableConfig.getString("ts2022Q3TableName")

  val ts2022QATableName: String   = pgTableConfig.getString("ts2022QATableName")
  val ts2022Q1QATableName: String = pgTableConfig.getString("ts2022Q1QATableName")
  val ts2022Q2QATableName: String = pgTableConfig.getString("ts2022Q2QATableName")
  val ts2022Q3QATableName: String = pgTableConfig.getString("ts2022Q3QATableName")

  //common table names
  val emailTableName: String = pgTableConfig.getString("emailTableName")

}