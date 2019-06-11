package hmda.data.browser.repositories
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models.{BrowserField, ModifiedLarEntity}
import monix.eval.Task
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcProfile, ResultSetConcurrency, ResultSetType}
import slick.util.SQLBuilder

class PostgresModifiedLarRepository(tableName: String,
                                    config: DatabaseConfig[JdbcProfile])
  extends ModifiedLarRepository {
  import config._
  import config.profile.api._

  private val columns: String =
    """id,
    	lei,
    	loan_type,
    	loan_purpose,
    	preapproval,
    	construction_method,
    	occupancy_type,
    	loan_amount,
    	action_taken_type,
    	state,
    	county,
    	tract,
      ethnicity_applicant_1,
      ethnicity_applicant_2,
      ethnicity_applicant_3,
      ethnicity_applicant_4,
      ethnicity_applicant_5,
      ethnicity_observed_applicant,
      ethnicity_co_applicant_1,
      ethnicity_co_applicant_2,
      ethnicity_co_applicant_3,
      ethnicity_co_applicant_4,
      ethnicity_co_applicant_5,
      ethnicity_observed_co_applicant,
      race_applicant_1,
      race_applicant_2,
      race_applicant_3,
      race_applicant_4,
      race_applicant_5,
      race_co_applicant_1,
      race_co_applicant_2,
      race_co_applicant_3,
      race_co_applicant_4,
      race_co_applicant_5,
      race_observed_applicant,
      race_observed_co_applicant,
      sex_applicant,
      sex_co_applicant,
      observed_sex_applicant,
      observed_sex_co_applicant,
      age_applicant,
      applicant_age_greater_than_62,
      age_co_applicant,
      coapplicant_age_greater_than_62,
      income,
      purchaser_type,
      rate_spread,
      hoepa_status,
      lien_status,
      credit_score_type_applicant,
      credit_score_type_co_applicant,
      denial_reason1,
      denial_reason2,
      denial_reason3,
      denial_reason4,
      total_loan_costs,
      total_points,
      origination_charges,
      discount_points,
      lender_credits,
      interest_rate,
      payment_penalty,
      debt_to_income,
      loan_value_ratio,
      loan_term,
      rate_spread_intro,
      baloon_payment,
      insert_only_payment,
      amortization,
      other_amortization,
      property_value,
      home_security_policy,
      land_property_interest,
      total_units,
      mf_affordable,
      application_submission,
      payable,
      aus1,
      aus2,
      aus3,
      aus4,
      aus5,
      reverse_mortgage,
      line_of_credits,
      business_or_commercial,
      population,
      minority_population_percent,
      ffiec_med_fam_income,
      tract_to_msamd,
      owner_occupied_units,
      one_to_four_fam_units,
      msa_md,
      loan_flag,
      created_at,
      submission_id,
      msa_md_name,
      filing_year,
      conforming_loan_limit,
      median_age,
      median_age_calculated,
      median_income_percentage,
      race_categorization,
      sex_categorization,
      ethnicity_categorization,
      percent_median_msa_income
    """.stripMargin

  def formatString(strs: Seq[String]): String =
    strs.map(each => s"\'$each\'").mkString(start = "(", sep = ",", end = ")")

  def eq(fieldName: String, value: String): String =
    s"$fieldName = $value"

  def in(fieldName: String, values: Seq[String]): String =
    s"$fieldName IN ${formatString(values)}"

  def whereAndOpt(expression: String, remainingExpressions: String*): String = {
    val primary = s"WHERE $expression"
    if (remainingExpressions.isEmpty) primary
    else {
      val secondaries =
        remainingExpressions.map(expr => s"AND $expr").mkString(sep = " ")
      s"$primary $secondaries"
    }
  }

  override def find(
                     msaMd: Int,
                     field1: BrowserField,
                     field2: BrowserField): Source[ModifiedLarEntity, NotUsed] = {
    val searchQuery = {
      val remaining: List[String] = {
        val insertA =
          if (field1.name != "empty") Option(in(field1.dbName, field1.value))
          else None

        val insertB =
          if (field2.name != "empty") Option(in(field2.dbName, field2.value))
          else None

        List(insertA.toList, insertB.toList).flatten
      }

      val filterCriteria =
        whereAndOpt(eq("msa_md", msaMd.toString), remaining: _*)

      sql"""
      SELECT #$columns
      FROM #${tableName}
      #$filterCriteria
      """
        .as[ModifiedLarEntity]
        .withStatementParameters(
          rsType = ResultSetType.ForwardOnly,
          rsConcurrency = ResultSetConcurrency.ReadOnly,
          fetchSize = 1000
        )
        .transactionally
    }

    val publisher = db.stream(searchQuery)
    Source.fromPublisher(publisher)
  }

  override def find(
                     state: String,
                     field1: BrowserField,
                     field2: BrowserField): Source[ModifiedLarEntity, NotUsed] = {
    val searchQuery = {
      if (field1.name == "empty") {
        sql"""
      SELECT #$columns
      FROM #${tableName}
      WHERE state = '#${state}'
      """.as[ModifiedLarEntity]
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
          .transactionally
      } else if (field2.name == "empty") {
        sql"""
      SELECT #$columns
      FROM #${tableName}
      WHERE state = '#${state}'
        AND #${field1.dbName} IN #${formatString(field1.value)}
      """.as[ModifiedLarEntity]
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
          .transactionally
      } else {
        sql"""
      SELECT #$columns
      FROM #${tableName}
      WHERE state = '#${state}'
        AND #${field1.dbName} IN #${formatString(field1.value)}
        AND #${field2.dbName} IN #${formatString(field2.value)}
      """.as[ModifiedLarEntity]
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
          .transactionally
      }
    }

    val publisher = db.stream(searchQuery)
    Source.fromPublisher(publisher)
  }

  override def findAndAggregate(msaMd: Int,
                                field1DbName: String,
                                field1: String,
                                field2DbName: String,
                                field2: String): Task[Statistic] = {
    val query = {
      if (field1DbName == "empty") {
        sql"""SELECT
              COUNT(loan_amount),
              SUM(loan_amount)
            FROM #${tableName}
            WHERE msa_md = #${msaMd}
      """.as[Statistic].head
      } else if (field2DbName == "empty") {
        sql"""SELECT
              COUNT(loan_amount),
              SUM(loan_amount)
            FROM #${tableName}
            WHERE msa_md = #${msaMd}
              AND #${field1DbName} = '#${field1}'
      """.as[Statistic].head
      } else {
        sql"""SELECT
              COUNT(loan_amount),
              SUM(loan_amount)
            FROM #${tableName}
            WHERE msa_md = #${msaMd}
              AND #${field1DbName} = '#${field1}'
              AND #${field2DbName} = '#${field2}'
      """.as[Statistic].head
      }
    }

    // Slick shifts the execution context of the computation to its own pool (without shifting it back) and we need to
    // explicitly shift it back to Monix's default scheduler so we don't end up taking up resources on the thread pool
    // belonging to Slick since Monix Tasks don't shift async boundaries on every single call (which is what makes them
    // so performant) (taskA productL taskB) or (taskA <* taskB) means execute both taskA and taskB but take the result
    // of taskA (left computation hence productL)
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  override def findAndAggregate(state: String,
                                field1DbName: String,
                                field1: String,
                                field2DbName: String,
                                field2: String): Task[Statistic] = {
    val query = {
      if (field1DbName == "empty") {
        sql"""SELECT
              COUNT(loan_amount),
              SUM(loan_amount)
            FROM #${tableName}
            WHERE state = '#${state}'
      """.as[Statistic].head
      } else if (field2DbName == "empty") {
        sql"""SELECT
              COUNT(loan_amount),
              SUM(loan_amount)
            FROM #${tableName}
            WHERE state = '#${state}'
              AND #${field1DbName} = '#${field1.value}'
      """.as[Statistic].head
      } else {
        sql"""SELECT
              COUNT(loan_amount),
              SUM(loan_amount)
            FROM #${tableName}
            WHERE state = '#${state}'
              AND #${field1DbName} = '#${field1.value}'
              AND #${field2DbName} = '#${field2.value}'
      """.as[Statistic].head
      }
    }

    Task.deferFuture(db.run(query))
  }

  override def find(
                     field1: BrowserField,
                     field2: BrowserField): Source[ModifiedLarEntity, NotUsed] = {
    val searchQuery = {
      if (field1.name == "empty") {
        sql"""
    SELECT #$columns
    FROM #${tableName}
    """.as[ModifiedLarEntity]
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
          .transactionally
      } else if (field2.name == "empty") {
        sql"""
    SELECT #$columns
    FROM #${tableName}
    WHERE #${field1.dbName} IN #${formatString(field1.value)}
    """.as[ModifiedLarEntity]
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
          .transactionally
      } else {
        sql"""
    SELECT #$columns
    FROM #${tableName}
    WHERE #${field1.dbName} IN #${formatString(field1.value)}
      AND #${field2.dbName} IN #${formatString(field2.value)}
    """.as[ModifiedLarEntity]
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
          .transactionally
      }
    }

    val publisher = db.stream(searchQuery)
    Source.fromPublisher(publisher)
  }

  override def findAndAggregate(field1DbName: String,
                                field1: String,
                                field2DbName: String,
                                field2: String): Task[Statistic] = {
    val query = {
      if (field1DbName == "empty") {
        sql"""SELECT
              COUNT(loan_amount),
              SUM(loan_amount)
            FROM #${tableName}
      """.as[Statistic].head
      } else if (field2DbName == "empty") {
        sql"""SELECT
              COUNT(loan_amount),
              SUM(loan_amount)
            FROM #${tableName}
            WHERE #${field1DbName} = '#${field1}'
      """.as[Statistic].head
      } else {
        sql"""SELECT
              COUNT(loan_amount),
              SUM(loan_amount)
            FROM #${tableName}
            WHERE #${field1DbName} = '#${field1}'
            AND #${field2DbName} = '#${field2}'
      """.as[Statistic].head
      }
    }

    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  override def find(
                     msaMd: Int,
                     state: String,
                     field1: BrowserField,
                     field2: BrowserField): Source[ModifiedLarEntity, NotUsed] = {
    val searchQuery = {
      if (field1.name == "empty") {
        sql"""SELECT #$columns
                        FROM #${tableName}
                        WHERE msa_md = #${msaMd}
                        AND state = '#${state}'
    """.as[ModifiedLarEntity]
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
          .transactionally
      } else if (field2.name == "empty") {
        sql"""SELECT #$columns
                        FROM #${tableName}
                        WHERE msa_md = #${msaMd}
                        AND state = '#${state}'
                        AND #${field1.dbName} IN #${formatString(field1.value)}
    """.as[ModifiedLarEntity]
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
          .transactionally
      } else {
        sql"""SELECT #$columns
                        FROM #${tableName}
                        WHERE msa_md = #${msaMd}
                        AND state = '#${state}'
                        AND #${field1.dbName} IN #${formatString(field1.value)}
                        AND #${field2.dbName} IN #${formatString(field2.value)}
    """.as[ModifiedLarEntity]
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
          .transactionally
      }
    }

    val publisher = db.stream(searchQuery)
    Source.fromPublisher(publisher)
  }

  override def findAndAggregate(msaMd: Int,
                                state: String,
                                field1DbName: String,
                                field1: String,
                                field2DbName: String,
                                field2: String): Task[Statistic] = {
    val query = {
      if (field1DbName == "empty") {
        sql"""SELECT
        COUNT(loan_amount),
        SUM(loan_amount)
      FROM #${tableName}
      WHERE msa_md = #${msaMd}
      AND state = '#${state}'
      """.as[Statistic].head
      } else if (field2DbName == "empty") {
        sql"""SELECT
        COUNT(loan_amount),
        SUM(loan_amount)
      FROM #${tableName}
      WHERE msa_md = #${msaMd}
      AND state = '#${state}'
      AND #${field1DbName} = '#${field1}'
      """.as[Statistic].head
      } else {
        sql"""SELECT
        COUNT(loan_amount),
        SUM(loan_amount)
      FROM #${tableName}
      WHERE msa_md = #${msaMd}
      AND state = '#${state}'
      AND #${field1DbName} = '#${field1}'
      AND #${field2DbName} = '#${field2}'
      """.as[Statistic].head
      }
    }

    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }
}