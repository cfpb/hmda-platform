package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json.{ JsArray, JsString, _ }

class A4XSpec
    extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  def propType = Gen.oneOf(1, 2).sample.get

  val fips = 18700 //Corvallis, OR
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(loanType = 1, propertyType = propType, purpose = 1)
    lar.copy(geography = geo, loan = loan)
  }

  val aggregateSource: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionA42 = "Disposition of applications for conventional home-purchase loans 1- to 4- family and manufactured home dwellings, by race, ethnicity, gender and income of applicant"
  val descriptionN46 = "Disposition of applications from nonoccupants for home-purchase, home improvement, or refinancing loans, 1- to 4- family and manufactured home dwellings, by race, ethnicity, gender and income of applicant"

  "Generate an Aggregate 4-2 report" in {
    A42.generate(aggregateSource, fips).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "A42"
        fipsCode mustBe fips.toString
        report.parseJson.asJsObject.getFields("table", "type", "description", "year", "msa") match {
          case Seq(JsString(table), JsString(reportType), JsString(desc), JsString(reportYear), msa) =>
            table mustBe "4-2"
            reportType mustBe "Aggregate"
            desc mustBe descriptionA42
            msa.asJsObject.getFields("name") match {
              case Seq(JsString(msaName)) => msaName mustBe "Corvallis, OR"
            }
        }
    }
  }

  "Include correct demographics for dispositions" in {
    A45.generate(aggregateSource, fips).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "A45"
        fipsCode mustBe fips.toString
        report.parseJson.asJsObject.getFields("races", "minorityStatuses", "ethnicities", "incomes", "total") match {
          case Seq(JsArray(races), JsArray(ms), JsArray(ethnicities), JsArray(incomes), JsArray(total)) =>
            races must have size 8
            ms must have size 2
            ethnicities must have size 4
            incomes must have size 6
            total must have size A42.dispositions.size
            races.head.asJsObject.getFields("race") match {
              case Seq(JsString(name)) => name mustBe "American Indian/Alaska Native"
            }
        }
    }
  }

  val lars2 = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val loan = lar.loan.copy(loanType = 1, propertyType = propType, purpose = 1)
    lar.copy(loan = loan)
  }

  val nationalSource: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars2.toIterator)

  "Generate a National Aggregate 4-6 report" in {
    N46.generate(nationalSource, fips).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "N46"
        fipsCode mustBe "nationwide"
        report.parseJson.asJsObject.getFields("table", "type", "description", "year") match {
          case Seq(JsString(table), JsString(reportType), JsString(desc), JsString(reportYear)) =>
            table mustBe "4-6"
            reportType mustBe "National Aggregate"
            desc mustBe descriptionN46
        }
    }
  }
}
