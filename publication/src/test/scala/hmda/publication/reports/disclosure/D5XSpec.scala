package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.institution.ExternalIdType.RssdId
import hmda.model.institution.{ ExternalId, Institution, Respondent }
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json._

class D5XSpec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val respId = "98765"
  val fips = 18700 //Corvallis, OR
  def propType = Gen.oneOf(1, 2).sample.get
  val resp = Respondent(ExternalId(respId, RssdId), "Corvallis Test Bank", "", "", "")
  val inst = Institution.empty.copy(respondent = resp)
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(loanType = 1, propertyType = propType, purpose = 1)
    lar.copy(respondentId = respId, geography = geo, loan = loan)
  }

  val descriptionD51 = "Disposition of applications for FHA, FSA/RHS, and VA home-purchase loans, 1- to 4-family and manufactured home dwellings, by income, race and ethnicity of applicant"

  val source: Source[LoanApplicationRegister, NotUsed] = Source.fromIterator(() => lars.toIterator)

  "Generate a Disclosure 5-1 report" in {
    D51.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("respondentId", "institutionName", "table", "description", "msa", "type", "reportDate") match {
        case Seq(JsString(respondentId), JsString(instName), JsString(table), JsString(desc), msa, JsString(reportType), JsString(reportDate)) =>
          respondentId mustBe respId
          instName mustBe "Corvallis Test Bank"
          table mustBe "5-1"
          desc mustBe descriptionD51
          reportType mustBe "Disclosure"
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Corvallis, OR"
          }
      }
    }
  }

  "Have correct ApplicantIncomes JSON structure" in {
    D52.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("applicantIncomes") match {
        case Seq(JsArray(incomes)) =>
          incomes.size mustBe 5
          incomes.head.asJsObject.getFields("applicantIncome", "borrowerCharacteristics") match {
            case Seq(JsString(categoryName), JsArray(characteristics)) =>
              categoryName mustBe "Less than 50% of MSA/MD median"
              characteristics.size mustBe 3
              characteristics.head.asJsObject.getFields("characteristic", "races") match {
                case Seq(JsString(characteristic), JsArray(races)) =>
                  characteristic mustBe "Race"
                  races.size mustBe 8
                  races.head.asJsObject.getFields("race", "dispositions") match {
                    case Seq(JsString(race), JsArray(disp)) =>
                      race mustBe "American Indian/Alaska Native"
                      disp.size mustBe 6
                  }
              }
          }
      }
    }
  }

  "Have correct totals JSON" in {
    D53.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("total") match {
        case Seq(JsArray(totals)) =>
          totals.size mustBe 6
          totals.head.asJsObject.getFields("disposition", "count", "value") match {
            case Seq(JsString(disp), JsNumber(count), JsNumber(value)) =>
              disp mustBe "Applications Received"
          }
      }
    }
  }

}
