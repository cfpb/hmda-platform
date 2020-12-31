package hmda.reporting.api.http

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import hmda.model.institution.{ HmdaFiler, HmdaFilerResponse, MsaMd, MsaMdResponse }
import hmda.query.institution.InstitutionEntity
import hmda.query.repository.ModifiedLarRepository
import hmda.reporting.repository.InstitutionComponent
import hmda.utils.EmbeddedPostgres
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.Await
import scala.concurrent.duration._

class ReportingHttpApiSpec
  extends WordSpec
    with ScalatestRouteTest
    with ScalaCheckPropertyChecks
    with FailFastCirceSupport
    with EmbeddedPostgres
    with InstitutionComponent
    with Matchers
    with ScalaFutures {
  import dbConfig._
  import dbConfig.profile.api._

  val institutionRepo2018 = new InstitutionRepository(dbConfig, "institutions2018")
  val institutionRepo2019 = new InstitutionRepository(dbConfig, "institutions2019")
  val mlarRepo            = new ModifiedLarRepository(dbConfig)

  override def bootstrapSqlFile: String = "modifiedlar.sql"

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(institutionRepo2018.createSchema(), 30.seconds)
    Await.ready(institutionRepo2019.createSchema(), 30.seconds)
  }

  val routes: Route = ReportingHttpApi.create(system.settings.config)

  "Reporting HTTP API" must {
    "respond to filers/<year>" in {
      whenReady(
        db.run(
          (institutionRepo2018.institutionsTable += InstitutionEntity(lei = "EXAMPLE-LEI-1", activityYear = 2018, hmdaFiler = true)) >>
            (institutionRepo2019.institutionsTable += InstitutionEntity(lei = "EXAMPLE-LEI-2", activityYear = 2019, hmdaFiler = true))
        )
      )(_ shouldBe 1)

      Get("/filers/2018") ~> routes ~> check {
        response.status shouldBe OK
        responseAs[HmdaFilerResponse] shouldBe HmdaFilerResponse(Set(HmdaFiler("EXAMPLE-LEI-1", "", 2018.toString)))
      }

      Get("/filers/2019") ~> routes ~> check {
        response.status shouldBe OK
        responseAs[HmdaFilerResponse] shouldBe HmdaFilerResponse(Set(HmdaFiler("EXAMPLE-LEI-2", "", 2019.toString)))
      }

      Get("/filers/1111") ~> routes ~> check {
        response.status shouldBe OK
        responseAs[HmdaFilerResponse] shouldBe HmdaFilerResponse(Set(HmdaFiler("", "", "")))
      }
    }

    "respond to filers/<year>/<lei>/msaMds" in {
      executeSQL(
        sql"INSERT INTO modifiedlar2018 (id, lei, loan_type, loan_purpose, preapproval, construction_method, occupancy_type, loan_amount, action_taken_type, state, county, tract, ethnicity_applicant_1, ethnicity_applicant_2, ethnicity_applicant_3, ethnicity_applicant_4, ethnicity_applicant_5, ethnicity_observed_applicant, ethnicity_co_applicant_1, ethnicity_co_applicant_2, ethnicity_co_applicant_3, ethnicity_co_applicant_4, ethnicity_co_applicant_5, ethnicity_observed_co_applicant, race_applicant_1, race_applicant_2, race_applicant_3, race_applicant_4, race_applicant_5, race_co_applicant_1, race_co_applicant_2, race_co_applicant_3, race_co_applicant_4, race_co_applicant_5, race_observed_applicant, race_observed_co_applicant, sex_applicant, sex_co_applicant, observed_sex_applicant, observed_sex_co_applicant, age_applicant, applicant_age_greater_than_62, age_co_applicant, coapplicant_age_greater_than_62, income, purchaser_type, rate_spread, hoepa_status, lien_status, credit_score_type_applicant, credit_score_type_co_applicant, denial_reason1, denial_reason2, denial_reason3, denial_reason4, total_loan_costs, total_points, origination_charges, discount_points, lender_credits, interest_rate, payment_penalty, debt_to_incode, loan_value_ratio, loan_term, rate_spread_intro, baloon_payment, insert_only_payment, amortization, other_amortization, property_value, home_security_policy, lan_property_interest, total_units, mf_affordable, application_submission, payable, aus1, aus2, aus3, aus4, aus5, reverse_mortgage, line_of_credits, business_or_commercial, population, minority_population_percent, ffiec_med_fam_income, tract_to_msamd, owner_occupied_units, one_to_four_fam_units, msa_md, loan_flag, created_at, submission_id, msa_md_name, filing_year, conforming_loan_limit, median_age, median_age_calculated, median_income_percentage, race_categorization, sex_categorization, ethnicity_categorization, uniq_id, percent_median_msa_income) VALUES (2, 'EXAMPLE-LEI-10', 3, 5, 2, '2', 1, 25000, 2, 'AL', '01049', '01049961200', '3', NULL, NULL, NULL, NULL, 3, '1', '2', NULL, NULL, NULL, 1, '3', '23', '25', '24', '26', '8', NULL, NULL, NULL, NULL, 2, 4, 3, 3, 3, 3, '35-44', 'No', '25-34', 'No', 'NA', 0, '89.89', 3, 1, 8, 8, 10, NULL, NULL, NULL, 'NA', 'NA', 'NA', 'NA', 'NA', '8.08', '15', 'NA', '49.0', '317', '2', 1111, 1111, 1111, 1111, '725000', 3, 5, '5-24', '71', 2, 3, 1111, NULL, NULL, NULL, NULL, 2, 2, 1111, '2190', '16.8000000000000007', '48500', '961200', '753', '880', 99999, NULL, '2019-04-24 00:12:49.043869', 'B90YWS6AFX2LGWOXJ1LD-2018-2', 'NA', 2018, 'NA', 25, '1990 - 1999', 149, '2 or more minority races', 'Sex not available', 'Ethnicity Not Available', 1600139, NULL)".asUpdate
      )
      executeSQL(
        sql"INSERT INTO modifiedlar2019 (id, lei, loan_type, loan_purpose, preapproval, construction_method, occupancy_type, loan_amount, action_taken_type, state, county, tract, ethnicity_applicant_1, ethnicity_applicant_2, ethnicity_applicant_3, ethnicity_applicant_4, ethnicity_applicant_5, ethnicity_observed_applicant, ethnicity_co_applicant_1, ethnicity_co_applicant_2, ethnicity_co_applicant_3, ethnicity_co_applicant_4, ethnicity_co_applicant_5, ethnicity_observed_co_applicant, race_applicant_1, race_applicant_2, race_applicant_3, race_applicant_4, race_applicant_5, race_co_applicant_1, race_co_applicant_2, race_co_applicant_3, race_co_applicant_4, race_co_applicant_5, race_observed_applicant, race_observed_co_applicant, sex_applicant, sex_co_applicant, observed_sex_applicant, observed_sex_co_applicant, age_applicant, applicant_age_greater_than_62, age_co_applicant, coapplicant_age_greater_than_62, income, purchaser_type, rate_spread, hoepa_status, lien_status, credit_score_type_applicant, credit_score_type_co_applicant, denial_reason1, denial_reason2, denial_reason3, denial_reason4, total_loan_costs, total_points, origination_charges, discount_points, lender_credits, interest_rate, payment_penalty, debt_to_incode, loan_value_ratio, loan_term, rate_spread_intro, baloon_payment, insert_only_payment, amortization, other_amortization, property_value, home_security_policy, lan_property_interest, total_units, mf_affordable, application_submission, payable, aus1, aus2, aus3, aus4, aus5, reverse_mortgage, line_of_credits, business_or_commercial, population, minority_population_percent, ffiec_med_fam_income, tract_to_msamd, owner_occupied_units, one_to_four_fam_units, msa_md, loan_flag, created_at, submission_id, msa_md_name, filing_year, conforming_loan_limit, median_age, median_age_calculated, median_income_percentage, race_categorization, sex_categorization, ethnicity_categorization, uniq_id, percent_median_msa_income) VALUES (2, 'EXAMPLE-LEI-20', 1, 4, 2, '2', 1, 435000, 6, 'IL', '17031', '17031813100', '3', NULL, NULL, NULL, NULL, 3, '5', NULL, NULL, NULL, NULL, 4, '7', NULL, NULL, NULL, NULL, '26', NULL, NULL, NULL, '24', 3, 2, 4, 1, 3, 1, '25-34', 'No', '<25', 'No', 'NA', 0, 'NA', 3, 2, 9, 9, 10, NULL, NULL, NULL, 'NA', 'NA', 'NA', 'NA', 'NA', 'NA', 'NA', 'NA', 'NA', 'NA', '9', 1111, 1111, 1111, 1111, '485000', 3, 5, '5-24', '100', 3, 3, 6, NULL, NULL, NULL, NULL, 1, 1, 1111, '4938', '49.8599999999999994', '82400', '813100', '1106', '1409', 16974, NULL, '2019-04-24 00:14:40.530774', 'BANK1LEIFORTEST12345-2018-1', 'NA', 2019, 'NA', 76, '1969 or Earlier', 136, 'Race not available', 'Sex not available', 'Ethnicity Not Available', 1605094, NULL)".asUpdate
      )

      whenReady(
        db.run(
          (institutionRepo2018.institutionsTable += InstitutionEntity(lei = "EXAMPLE-LEI-10", activityYear = 2018, hmdaFiler = true)) >>
            (institutionRepo2019.institutionsTable += InstitutionEntity(lei = "EXAMPLE-LEI-20", activityYear = 2019, hmdaFiler = true))
        )
      )(_ shouldBe 1)

      Get("/filers/2018/EXAMPLE-LEI-10/msaMds") ~> routes ~> check {
        response.status shouldBe OK

        import io.circe.generic.auto._
        responseAs[MsaMdResponse] shouldBe MsaMdResponse(HmdaFiler("EXAMPLE-LEI-10", "", "2018"), Set(MsaMd("99999", "NA")))
      }

      Get("/filers/2019/EXAMPLE-LEI-20/msaMds") ~> routes ~> check {
        response.status shouldBe OK

        import io.circe.generic.auto._
        responseAs[MsaMdResponse] shouldBe MsaMdResponse(HmdaFiler("EXAMPLE-LEI-20", "", "2019"), Set(MsaMd("16974", "NA")))
      }
    }
  }
}