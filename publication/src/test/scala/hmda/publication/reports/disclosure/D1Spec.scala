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

class D1Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

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
    lar.copy(respondentId = respId)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionD1 = "Disposition of loan applications, by location of property and type of loan"

  "Generate a Disclosure 1 report" in {
    D1.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("respondentId", "institutionName", "table", "description", "msa") match {
        case Seq(JsString(respondentId), JsString(instName), JsString(table), JsString(desc), msa) =>
          respondentId mustBe respId
          instName mustBe "Fox Valley Test Bank"
          table mustBe "1"
          desc mustBe descriptionD1
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Appleton, WI"
          }
      }
    }
  }

  "Include correct structure" in {
    D1.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("tracts") match {

        case Seq(JsArray(characteristics)) =>
          characteristics.head.asJsObject.getFields("tract", "dispositions") match {

            case Seq(JsString(char), JsArray(comps)) =>
              char.split("/").size mustBe 3
              comps must have size 5
              comps.head.asJsObject.getFields("title", "values") match {

                case Seq(JsString(comp), JsArray(disp)) =>
                  comp mustBe "Loans originated"
                  disp must have size 7
              }
          }
      }
    }
  }
}

