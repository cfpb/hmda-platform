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

class D7XSpec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val respId = "54345"
  val fips = 11540 // Appleton, WI
  val resp = Respondent(ExternalId(respId, RssdId), "Fox Valley Test Bank", "", "", "")
  val inst = Institution.empty.copy(respondent = resp)
  val lars = Gen.listOfN(100, larWithValidGeoGen).sample.get.map { lar: LoanApplicationRegister =>
    val loan = lar.loan.copy(loanType = 2, purpose = 3, propertyType = 1, occupancy = 1)
    lar.copy(respondentId = respId, loan = loan, lienStatus = 1)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionD74 = "Disposition of applications for home improvement loans, 1- to 4-family and manufactured home dwellings, by characteristics of census tract in which property is located"

  "Generate a Disclosure 7-5 report" in {
    D74.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("respondentId", "institutionName", "table", "description", "msa") match {
        case Seq(JsString(respondentId), JsString(instName), JsString(table), JsString(desc), msa) =>
          respondentId mustBe respId
          instName mustBe "Fox Valley Test Bank"
          table mustBe "7-4"
          desc mustBe descriptionD74
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Appleton, WI"
          }
      }
    }
  }

  "Include correct Census Tract Characteristics" in {
    D75.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("censusTractCharacteristics") match {

        case Seq(JsArray(characteristics)) =>
          characteristics must have size 2
          characteristics.head.asJsObject.getFields("characteristic", "compositions") match {

            case Seq(JsString(char), JsArray(comps)) =>
              char mustBe "Racial/Ethnic Composition"
              comps must have size 5
              comps.head.asJsObject.getFields("composition", "dispositions") match {

                case Seq(JsString(comp), JsArray(disp)) =>
                  comp mustBe "Less than 10% minority"
                  disp must have size 6
              }
          }
      }
    }
  }

  "Include correct Income/Racial Composition json" in {
    D76.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("incomeRaces") match {

        case Seq(JsArray(incomeRaces)) =>
          incomeRaces must have size 1
          incomeRaces.head.asJsObject.getFields("characteristic", "incomes") match {

            case Seq(JsString(char), JsArray(incomes)) =>
              char mustBe "Income & Racial/Ethnic Composition"
              incomes must have size 4
              incomes.head.asJsObject.getFields("income", "compositions") match {

                case Seq(JsString(income), JsArray(comps)) =>
                  income mustBe "Low income"
                  comps must have size 5
                  comps.head.asJsObject.getFields("composition", "dispositions") match {

                    case Seq(JsString(comp), JsArray(disp)) =>
                      comp mustBe "Less than 10% minority"
                      disp must have size 6
                  }
              }
          }
      }
    }
  }

  "Include correct remaining json" in {
    D76.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("types", "total") match {

        case Seq(JsArray(types), JsArray(total)) =>
          total must have size 6
          types must have size 2
          types.head.asJsObject.getFields("type", "dispositions") match {

            case Seq(JsString(typ), JsArray(disp)) =>
              typ mustBe "Small County"
              disp must have size 6
              disp.head.asJsObject.getFields("disposition", "count", "value") match {

                case Seq(JsString(d), JsNumber(c), JsNumber(v)) =>
                  d mustBe "Applications Received"
                  c <= 100 mustBe true
                  v >= 0 mustBe true
              }
          }
      }
    }
  }
}

