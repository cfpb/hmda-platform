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

class DisclosureIRSSpec extends AsyncWordSpec with MustMatchers
    with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val respId = "65656"
  val resp = Respondent(ExternalId(respId, RssdId), "Grand Junction Mortgage Co.", "", "", "")
  val inst = Institution.empty.copy(respondent = resp)
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    lar.copy(respondentId = respId)
  }

  val msaList = lars.map(_.geography.msa).distinct.filterNot(_ == "NA").map(_.toInt)

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val description = "Home Mortgage Disclosure Act Institution Register Summary for 2017"

  "Generate an IRS report" in {
    DIRS.generate(source, -1, inst, msaList).map { result =>
      println(result.report)
      result.report.parseJson.asJsObject.getFields("table", "description", "respondentId", "institutionName") match {
        case Seq(JsString(table), JsString(desc), JsString(resp), JsString(instName)) =>
          table mustBe "IRS"
          desc mustBe description
          resp mustBe "65656"
          instName mustBe "Grand Junction Mortgage Co."
      }
    }
  }

  "Have correct JSON structure" in {
    DIRS.generate(source, -1, inst, msaList).map { result =>
      result.report.parseJson.asJsObject.getFields("msas", "total") match {
        case Seq(JsArray(msas), total) =>
          msas must have size msaList.size + 1
          total.asJsObject.getFields("msaName", "totalLars") match {
            case Seq(JsString(msaName), JsNumber(totalLars)) =>
              msaName mustBe "Total"
              totalLars mustBe 100
          }
      }
    }
  }

}
