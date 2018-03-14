package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.publication.reports.util.DispositionType
import hmda.publication.reports.util.DispositionType._
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

class A52Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val fips = 18700 //Corvallis, OR
  def propType = Gen.oneOf(1, 2).sample.get

  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(loanType = 1, propertyType = propType, purpose = 1)
    lar.copy(geography = geo, loan = loan)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val expectedDispositions: List[DispositionType] =
    List(ApplicationReceived, LoansOriginated, ApprovedButNotAccepted, ApplicationsDenied, ApplicationsWithdrawn, ClosedForIncompleteness)

  /*
  "Generate correct JSON structure" in {
    A52.generate(source, fips).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "A52"
        fipsCode mustBe fips.toString
        report.parseJson.asJsObject.getFields("table", "type", "description", "year", "msa", "reportDate") match {
          case Seq(JsString(table), JsString(reportType), JsString(desc), JsNumber(reportYear), msa, JsString(reportDate)) =>
            table mustBe "5-2"
            reportType mustBe "Aggregate"
            desc mustBe a52Description
            msa.asJsObject.getFields("name") match {
              case Seq(JsString(msaName)) => msaName mustBe "Corvallis, OR"
            }
        }
    }
  }

  "Generate correct Applicant Incomes Json structure" in {
    A52.generate(source, fips).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        report.parseJson.asJsObject.getFields("applicantIncomes", "total") match {
          case Seq(JsArray(incomes), JsArray(tot)) =>
            incomes.size mustBe 5
            tot.size mustBe 6
        }
    }
  }
  */

}
