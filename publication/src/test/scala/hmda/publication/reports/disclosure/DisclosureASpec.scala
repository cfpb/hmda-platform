package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.institution.ExternalIdType.RssdId
import hmda.model.institution.{ ExternalId, Institution, Respondent }
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json._

class DisclosureASpec extends AsyncWordSpec with MustMatchers
    with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val respId = "65656"
  val fips = 24300 // Grand Junction, CO
  val resp = Respondent(ExternalId(respId, RssdId), "Grand Junction Mortgage Co.", "", "", "")
  val inst = Institution.empty.copy(respondent = resp)
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(loanType = 1, propertyType = 1)
    lar.copy(respondentId = respId, geography = geo, loan = loan)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val description = "Disposition of applications and loan sales by loan type, 1- to 4-family dwellings (Excludes manufactured homes)"

  "Generate an A1 report" in {
    A1.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("table", "description", "msa", "respondentId", "institutionName") match {
        case Seq(JsString(table), JsString(desc), msa, JsString(resp), JsString(instName)) =>
          table mustBe "A1"
          desc mustBe description
          resp mustBe "65656"
          instName mustBe "Grand Junction Mortgage Co."
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Grand Junction, CO"
          }
      }
    }
  }

  "Have correct JSON structure" in {
    A1.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("dispositions") match {
        case Seq(JsArray(dispositions)) =>
          dispositions must have size 8

          dispositions.head.asJsObject.getFields("disposition", "loanTypes") match {
            case Seq(JsString(char), JsArray(loanTypes)) =>
              char mustBe "Applications Received"
              loanTypes must have size 4

              loanTypes.head.asJsObject.getFields("loanType", "purposes") match {
                case Seq(JsString(conv), JsArray(purposes)) =>
                  conv mustBe "Conventional"
                  purposes must have size 3

                  purposes.head.asJsObject.getFields("purpose", "firstLienCount", "juniorLienCount") match {
                    case Seq(JsString(purpose), JsNumber(first), JsNumber(junior)) =>
                      purpose mustBe "Home Purchase"
                  }
              }
          }
      }
    }
  }

}
