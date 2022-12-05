package hmda.dataBrowser.repositories

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models.{ FilerInformation2017, ModifiedLarEntity2017, QueryField, LarQueryField, Statistic }
import monix.eval.Task
import slick.basic.DatabaseConfig
import slick.jdbc.{ JdbcProfile, ResultSetConcurrency, ResultSetType }

// $COVERAGE-OFF$
class PostgresModifiedLarRepository2017(tableName: String, config: DatabaseConfig[JdbcProfile]) extends ModifiedLarRepository2017 {

  import config._
  import config.profile.api._

  private val columns: String =
    """id,
    respondent_id,
    agency,
    loan_type,
    property_type,
    loan_purpose,
    occupancy,
    loan_amount,
    preapproval,
    action_taken_type,
    msa_md,
    state,
    county,
    tract,
    ethnicity_applicant_1,
    ethnicity_co_applicant_1,
    race_applicant_1,
    race_applicant_2,
    race_applicant_3,
    race_applicant_4,
    race_applicant_5,
    co_race_applicant_1,
    co_race_applicant_2,
    co_race_applicant_3,
    co_race_applicant_4,
    co_race_applicant_5,
    sex_applicant,
    sex_co_applicant,
    income,
    purchaser_type,
    denial_reason1,
    denial_reason2,
    denial_reason3,
    rate_spread,
    hoepa_status,
    lien_status,
    population,
    minority_population_percent,
    ffiec_med_fam_income,
    tract_to_msa_income_pct,
    owner_occupied_units,
    one_to_four_fam_units,
    filing_year,
    arid
    """.stripMargin

  def escape(str: String): String = str.replace("'", "")

  def formatSeq(strs: Seq[String]): String =
    strs.map(each => s"\'$each\'").mkString(start = "(", sep = ",", end = ")")

  def eq(fieldName: String, value: String): String =
    s"${escape(fieldName)} = '${escape(value)}'"

  def in(fieldName: String, values: Seq[String]): String =
    s"${escape(fieldName)} IN ${formatSeq(values.map(escape))}"

  def whereAndOpt(expression: String, remainingExpressions: String*): String = {
    val primary = s"WHERE $expression"
    if (remainingExpressions.isEmpty) primary
    else {
      val secondaries =
        remainingExpressions
        //do not include year in the WHERE clause because all entries in the table (modifiedlar2018_snapshot) have filing_year = 2018
          .map(expr => s"AND $expr")
          .mkString(sep = " ")
      s"$primary $secondaries"
    }
  }

  override def find(browserFields: List[QueryField], year: Int): Source[ModifiedLarEntity2017, NotUsed] = {
    val queries = browserFields.map(field => in(field.dbName, field.values))

    val filterCriteria = queries match {
      case Nil          => ""
      case head :: tail => whereAndOpt(head, tail: _*)
    }

    println(sql"""
      SELECT #${columns}
      FROM #${tableName}
      #$filterCriteria
      """)

    val searchQuery = sql"""
      SELECT #${columns}
      FROM #${tableName}
      #$filterCriteria
      """
      .as[ModifiedLarEntity2017]
      .withStatementParameters(
        rsType = ResultSetType.ForwardOnly,
        rsConcurrency = ResultSetConcurrency.ReadOnly,
        fetchSize = 1000
      )
      .transactionally

    val publisher = db.stream(searchQuery)
    Source.fromPublisher(publisher)
  }

  override def findFilers(filerFields: List[QueryField], year: Int): Task[Seq[FilerInformation2017]] = {
    val institutionsTableName = year match { //will be needed when databrowser has to support multiple years
      case 2017 => "transmittalsheet2017_public_ultimate"
      case _    => "transmittalsheet2017_public_ultimate"
    }
    //do not include year in the WHERE clause because all entries in the table (modifiedlar2018_snapshot) have filing_year = 2018
    val queries = filerFields.filterNot(_.name == "year").map(field => in(field.dbName, field.values))
    val filterCriteria = queries match {
      case Nil          => ""
      case head :: tail => whereAndOpt(head, tail: _*)
    }
    val query =
      sql"""
        SELECT a.arid, b.institution_name, a.lar_count, '#$year'
        from (
          SELECT arid, count(*) as lar_count
          FROM #${tableName}
          #$filterCriteria
          GROUP BY arid
        ) a
          JOIN #${institutionsTableName} b ON a.arid = b.arid
         """.as[FilerInformation2017]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  override def findAndAggregate(instQueryField: QueryField, geoQueryField: QueryField, hmdaQueryFields: List[LarQueryField], year: Int): Task[Statistic] = {
    val hmdaQueries = hmdaQueryFields.map(field => eq(field.dbName, field.value))
    val instQuery = in(instQueryField.dbName, instQueryField.values)
    val geoQuery = in(geoQueryField.dbName, geoQueryField.values)

    val queries = instQuery :: geoQuery :: hmdaQueries
    
    val filterCriteria = queries match {
      case Nil          => ""
      case head :: tail => whereAndOpt(head, tail: _*)
    }

    println("2017 db query")
    println(sql"""
        SELECT
          COUNT(loan_amount),
          SUM(loan_amount::numeric)
        FROM #${tableName}
        #$filterCriteria""")
    val query = sql"""
        SELECT
          COUNT(loan_amount),
          SUM(loan_amount::numeric)
        FROM #${tableName}
        #$filterCriteria
        """.as[Statistic].head

    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def healthCheck: Task[Unit] =
    Task.deferFuture(db.run(sql"SELECT 1".as[Int])).guarantee(Task.shift).void
}
// $COVERAGE-ON$