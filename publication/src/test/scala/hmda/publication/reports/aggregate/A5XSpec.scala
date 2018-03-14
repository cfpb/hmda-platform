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

class A5XSpec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

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

  val a51Desc = "Disposition of applications for FHA, FSA/RHS, and VA home-purchase loans, 1- to 4-family and manufactured home dwellings, by income, race and ethnicity of applicant"

  "Generate correct JSON structure" in {
    A51.generate(source, fips).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "A51"
        fipsCode mustBe fips.toString
        report.parseJson.asJsObject.getFields("table", "type", "description", "year", "msa", "reportDate") match {
          case Seq(JsString(table), JsString(reportType), JsString(desc), JsString(reportYear), msa, JsString(reportDate)) =>
            table mustBe "5-1"
            reportType mustBe "Aggregate"
            desc mustBe a51Desc
            msa.asJsObject.getFields("name") match {
              case Seq(JsString(msaName)) => msaName mustBe "Corvallis, OR"
            }
        }
    }
  }

  "Generate correct Applicant Incomes Json structure" in {
    A52.generate(source, fips).map { result =>
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
    A53.generate(source, fips).map { result =>
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

  val lars2 = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val loan = lar.loan.copy(propertyType = 2, purpose = 3)
    lar.copy(loan = loan)
  }

  val nationalSource: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars2.toIterator)

  val n57Desc = "Disposition of applications for home-purchase, home improvement, or refinancing loans, manufactured home dwellings, by income, race and ethnicity of applicant"

  "Generate a National Aggregate 5-7 report" in {
    N57.generate(nationalSource, -1).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "N56"
        fipsCode mustBe "nationwide"
        report.parseJson.asJsObject.getFields("table", "type", "description", "year") match {
          case Seq(JsString(table), JsString(reportType), JsString(desc), JsString(reportYear)) =>
            table mustBe "5-7"
            reportType mustBe "National Aggregate"
            desc mustBe n57Desc
        }
    }
  }
}
