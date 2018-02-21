package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.institution.ExternalIdType.RssdId
import hmda.model.institution.{ ExternalId, Institution, Respondent }
import hmda.publication.reports.util.DispositionType._
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json._

class D51Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val respId = "98765"
  val fips = 18700 //Corvallis, OR
  def loanType = Gen.oneOf(2, 3, 4).sample.get
  def propType = Gen.oneOf(1, 2).sample.get
  val resp = Respondent(ExternalId(respId, RssdId), "Corvallis Test Bank", "", "", "")
  val inst = Institution.empty.copy(respondent = resp)
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(loanType = loanType, propertyType = propType, purpose = 1)
    lar.copy(respondentId = respId, geography = geo, loan = loan)
  }

  val descriptionD51 = "Disposition of applications for FHA, FSA/RHS, and VA home-purchase loans, 1- to 4-family and manufactured home dwellings, by income, race and ethnicity of applicant"

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val expectedDispositionNames =
    List(ApplicationReceived, LoansOriginated, ApprovedButNotAccepted, ApplicationsDenied, ApplicationsWithdrawn, ClosedForIncompleteness)
      .map(_.value)

  "Generate a Disclosure 5-1 report" in {
    D51.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("respondentId", "institutionName", "table", "description", "msa") match {
        case Seq(JsString(respondentId), JsString(instName), JsString(table), JsString(desc), msa) =>
          respondentId mustBe respId
          instName mustBe "Corvallis Test Bank"
          table mustBe "5-1"
          desc mustBe descriptionD51
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Corvallis, OR"
          }
      }
    }
  }

}
