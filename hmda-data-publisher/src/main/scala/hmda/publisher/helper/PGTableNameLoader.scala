package hmda.publisher.helper

import com.typesafe.config.ConfigFactory

trait PGTableNameLoader {

  val pgTableConfig = ConfigFactory.load("application.conf").getConfig("pg-tables")

  //2018 table names
  val lar2018TableName: String   = pgTableConfig.getString("lar2018TableName")
  val mlar2018TableName: String  = pgTableConfig.getString("mlar2018TableName")
  val panel2018TableName: String = pgTableConfig.getString("panel2018TableName")
  val ts2018TableName: String    = pgTableConfig.getString("ts2018TableName")


  //2019 table names
  val lar2019TableName: String   = pgTableConfig.getString("lar2019TableName")
  val mlar2019TableName: String  = pgTableConfig.getString("mlar2019TableName")
  val panel2019TableName: String = pgTableConfig.getString("panel2019TableName")
  val ts2019TableName: String    = pgTableConfig.getString("ts2019TableName")


  //2020 table names
  val mlar2020TableName: String   = pgTableConfig.getString("mlar2020TableName")

  val panel2020TableName: String   = pgTableConfig.getString("panel2020TableName")

  val ts2020TableName: String   = pgTableConfig.getString("ts2020TableName")
  val ts2020Q1TableName: String = pgTableConfig.getString("ts2020Q1TableName")
  val ts2020Q2TableName: String = pgTableConfig.getString("ts2020Q2TableName")
  val ts2020Q3TableName: String = pgTableConfig.getString("ts2020Q3TableName")


  val lar2020TableName: String   = pgTableConfig.getString("lar2020TableName")
  val lar2020Q1TableName: String = pgTableConfig.getString("lar2020Q1TableName")
  val lar2020Q2TableName: String = pgTableConfig.getString("lar2020Q2TableName")
  val lar2020Q3TableName: String = pgTableConfig.getString("lar2020Q3TableName")


  //2021 table names
  val mlar2021TableName: String   = pgTableConfig.getString("mlar2021TableName")
  val mlar2022TableName: String   = pgTableConfig.getString("mlar2022TableName")
  val mlar2023TableName: String   = pgTableConfig.getString("mlar2023TableName")


  val panel2021TableName: String   = pgTableConfig.getString("panel2021TableName")

  val ts2021TableName: String   = pgTableConfig.getString("ts2021TableName")
  val ts2021Q1TableName: String = pgTableConfig.getString("ts2021Q1TableName")
  val ts2021Q2TableName: String = pgTableConfig.getString("ts2021Q2TableName")
  val ts2021Q3TableName: String = pgTableConfig.getString("ts2021Q3TableName")


  val lar2021TableName: String   = pgTableConfig.getString("lar2021TableName")
  val lar2021Q1TableName: String = pgTableConfig.getString("lar2021Q1TableName")
  val lar2021Q2TableName: String = pgTableConfig.getString("lar2021Q2TableName")
  val lar2021Q3TableName: String = pgTableConfig.getString("lar2021Q3TableName")



  val lar2022TableName: String   = pgTableConfig.getString("lar2022TableName")
  val lar2022Q1TableName: String = pgTableConfig.getString("lar2022Q1TableName")
  val lar2022Q2TableName: String = pgTableConfig.getString("lar2022Q2TableName")
  val lar2022Q3TableName: String = pgTableConfig.getString("lar2022Q3TableName")


  val panel2022TableName: String   = pgTableConfig.getString("panel2022TableName")

  val ts2022TableName: String   = pgTableConfig.getString("ts2022TableName")
  val ts2022Q1TableName: String = pgTableConfig.getString("ts2022Q1TableName")
  val ts2022Q2TableName: String = pgTableConfig.getString("ts2022Q2TableName")
  val ts2022Q3TableName: String = pgTableConfig.getString("ts2022Q3TableName")



  val lar2023TableName: String   = pgTableConfig.getString("lar2023TableName")
  val lar2023Q1TableName: String = pgTableConfig.getString("lar2023Q1TableName")
  val lar2023Q2TableName: String = pgTableConfig.getString("lar2023Q2TableName")
  val lar2023Q3TableName: String = pgTableConfig.getString("lar2023Q3TableName")

  val panel2023TableName: String   = pgTableConfig.getString("panel2023TableName")

  val ts2023TableName: String   = pgTableConfig.getString("ts2023TableName")
  val ts2023Q1TableName: String = pgTableConfig.getString("ts2023Q1TableName")
  val ts2023Q2TableName: String = pgTableConfig.getString("ts2023Q2TableName")
  val ts2023Q3TableName: String = pgTableConfig.getString("ts2023Q3TableName")


  //common table names
  val emailTableName: String = pgTableConfig.getString("emailTableName")
  val panelTableBase: String = pgTableConfig.getString("panelTableBase")
  val tsAnnualTableBase: String = pgTableConfig.getString("tsAnnualTableBase")
  val tsQuarterTableBase: String = pgTableConfig.getString("tsQuarterTableBase")
  val larAnnualTableBase: String = pgTableConfig.getString("larAnnualTableBase")
  val larQuarterTableBase: String = pgTableConfig.getString("larQuarterTableBase")
  val mLarTableBase: String = pgTableConfig.getString("mLarTableBase")
  val mLarAvailableYears: Seq[Int] = pgTableConfig.getString("mLarAvailableYears").split(",").map(s => s.toInt)
  val larAvailableYears: Seq[Int] = pgTableConfig.getString("larAvailableYears").split(",").map(s => s.toInt)
  val larQuarterAvailableYears: Seq[Int] = pgTableConfig.getString("larQuarterAvailableYears").split(",").map(s => s.toInt)
  val panelAvailableYears: Seq[Int] = pgTableConfig.getString("panelAvailableYears").split(",").map(s => s.toInt)
  val tsAvailableYears: Seq[Int] = pgTableConfig.getString("tsAvailableYears").split(",").map(s => s.toInt)
  val tsQuarterAvailableYears: Seq[Int] = pgTableConfig.getString("tsQuarterAvailableYears").split(",").map(s => s.toInt)

  // dynamic suffix parsing ({"ts": {"annual", {yr: "some_suffix"}}})
  val tableSuffixes: Map[String, Map[String, Map[String, String]]] = parseSuffixes()

  final object suffixKeys {
    final val TS = "ts"
    final val LAR = "lar"
    final val MLAR = "mlar"

    final val ANNUAL = "annual"
    final val Q1 = "q1"
    final val Q2 = "q2"
    final val Q3 = "q3"
  }

  import suffixKeys._

  def getSuffixes(year: Int, category: String) = {
    val yr = year.toString
    tableSuffixes.get(category) match {
      case Some(byPeriod) => (
        byPeriod.get(ANNUAL).flatMap(_.get(yr)).getOrElse(""),
        byPeriod.get(Q1).flatMap(_.get(yr)).getOrElse(""),
        byPeriod.get(Q2).flatMap(_.get(yr)).getOrElse(""),
        byPeriod.get(Q3).flatMap(_.get(yr)).getOrElse(""),
      )
    }
  }

  private def parseSuffixes() = {
    Map(
      TS -> parseCategory(TS),
      LAR -> parseCategory(LAR),
      MLAR -> parseCategory(MLAR)
    )
  }

  private def parseCategory(category: String) = {
    val tableSuffixes = pgTableConfig.getConfig("suffixes")
    val config = tableSuffixes.getConfig(category)
    category match {
      case MLAR => Map(
        ANNUAL -> parsePairs(config.getString(ANNUAL))
      )
      case _ => Map(
        ANNUAL -> parsePairs(config.getString(ANNUAL)),
        Q1 -> parsePairs(config.getString(Q1)),
        Q2 -> parsePairs(config.getString(Q2)),
        Q3 -> parsePairs(config.getString(Q3)),
      )
    }
  }

  private def parsePairs(str: String, pairSeparator: String = ",", entrySeparator: String = ":"): Map[String, String] = {
    if (str.trim.nonEmpty) {
      str.split(pairSeparator).flatMap(pair =>
        pair.split(entrySeparator, 2).grouped(2).map {
          case Array(k, v) => k -> v
          case _ => throw new IllegalArgumentException(s"Invalid suffix configuration found: $str")
        }).toMap
    } else {
      Map.empty
    }
  }

}