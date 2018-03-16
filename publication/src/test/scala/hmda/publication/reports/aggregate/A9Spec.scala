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
import spray.json._

class A9Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val fips = 11540 // Appleton, WI
  def actionTaken = Gen.choose(1, 5).sample.get

  val lars = Gen.listOfN(100, larWithValidGeoGen).sample.get.map { lar: LoanApplicationRegister =>
    lar.copy(actionTakenType = actionTaken)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val expectedDispositions: List[DispositionType] =
    List(ApplicationReceived, LoansOriginated, ApprovedButNotAccepted, ApplicationsDenied, ApplicationsWithdrawn, ClosedForIncompleteness)

  val description = "Disposition of loan applications, by median age of homes in census tract in which property is located and type of loan"

  "Generate correct JSON structure" in {
    A9.generate(source, fips).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "A9"
        fipsCode mustBe fips.toString
        report.parseJson.asJsObject.getFields("table", "type", "description", "year", "msa", "reportDate") match {
          case Seq(JsString(table), JsString(reportType), JsString(desc), JsString(reportYear), msa, JsString(reportDate)) =>
            table mustBe "9"
            reportType mustBe "Aggregate"
            desc mustBe description
            msa.asJsObject.getFields("name") match {
              case Seq(JsString(msaName)) => msaName mustBe "Appleton, WI"
            }
        }
    }
  }

  "Generate correct Median Ages JSON structure" in {
    A9.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("medianAges") match {
        case Seq(JsArray(ages)) =>
          ages.size mustBe 6
          ages.head.asJsObject.getFields("medianAge", "loanCategories") match {
            case Seq(JsString(medianAge), JsArray(loanCategories)) =>
              medianAge mustBe "2000 - 2010"
              loanCategories.size mustBe 7
              loanCategories.head.asJsObject.getFields("loanCategory", "dispositions") match {
                case Seq(JsString(loanCategory), JsArray(dispositions)) =>
                  loanCategory mustBe "FHA, FSA/RHS & VA (A)"
                  dispositions.size mustBe 5
              }
          }
      }
    }
  }

  val lars2 = lar100ListGen.sample.get

  val nationalSource: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars2.toIterator)

  "Generate a National Aggregate 9 report" in {
    N9.generate(nationalSource, -1).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "N9"
        fipsCode mustBe "nationwide"
        report.parseJson.asJsObject.getFields("table", "type", "description", "year") match {
          case Seq(JsString(table), JsString(reportType), JsString(desc), JsString(reportYear)) =>
            table mustBe "9"
            reportType mustBe "National Aggregate"
            desc mustBe description
        }
    }
  }
}
