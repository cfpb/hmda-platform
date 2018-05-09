package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json.{ JsArray, JsString, _ }

class A11XSpec
    extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  def oneTo3 = Gen.oneOf(1, 2, 3).sample.get
  def oneToo = Gen.oneOf(1, 2).sample.get

  val fips = 18700 //Corvallis, OR
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(loanType = oneTo3, propertyType = 1, purpose = oneTo3, occupancy = 1)
    lar.copy(geography = geo, loan = loan, lienStatus = oneToo)
  }

  val aggregateSource: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionA112 = "Pricing information for VA home-purchase loans, first lien, 1- to 4-family owner-occupied dwelling (excludes manufactured homes), by borrower or census tract characteristics"
  val descriptionN116 = "Pricing information for VA refinancing loans, first lien, 1- to 4-family owner-occupied dwelling (excludes manufactured homes), by borrower or census tract characteristics"

  "Generate an Aggregate 11-2 report" in {
    A11_2.generate(aggregateSource, fips).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "11-2"
        fipsCode mustBe fips.toString
        report.parseJson.asJsObject.getFields("table", "type", "description", "year", "msa") match {
          case Seq(JsString(table), JsString(reportType), JsString(desc), JsString(reportYear), msa) =>
            table mustBe "11-2"
            reportType mustBe "Aggregate"
            desc mustBe descriptionA112
            msa.asJsObject.getFields("name") match {
              case Seq(JsString(msaName)) => msaName mustBe "Corvallis, OR"
            }
        }
    }
  }

  "Include correct borrower Characteristics" in {
    A11_5.generate(aggregateSource, fips).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "11-5"
        fipsCode mustBe fips.toString
        report.parseJson.asJsObject.getFields("borrowerCharacteristics") match {
          case Seq(JsArray(characteristics)) =>
            characteristics must have size 5
            characteristics.head.asJsObject.getFields("characteristic", "races") match {

              case Seq(JsString(char), JsArray(races)) =>
                char mustBe "Race"
                races must have size 8
                races.head.asJsObject.getFields("race", "pricingInformation") match {

                  case Seq(JsString(race), JsArray(pricing)) =>
                    race mustBe "American Indian/Alaska Native"
                    pricing must have size 11
                }
            }
        }
    }
  }

  "Include correct Census Tract Characteristics" in {
    A11_5.generate(aggregateSource, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("censusTractCharacteristics") match {

        case Seq(JsArray(characteristics)) =>
          characteristics must have size 2
          characteristics.head.asJsObject.getFields("characteristic", "compositions") match {

            case Seq(JsString(char), JsArray(races)) =>
              char mustBe "Racial/Ethnic Composition"
              races must have size 5
              races.head.asJsObject.getFields("composition", "pricingInformation") match {

                case Seq(JsString(race), JsArray(pricing)) =>
                  race mustBe "Less than 10% minority"
                  pricing must have size 11
              }
          }
      }
    }
  }

  val lars2 = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val loan = lar.loan.copy(loanType = oneTo3, propertyType = 1, purpose = oneTo3, occupancy = 1)
    lar.copy(loan = loan, lienStatus = oneToo)
  }

  val nationalSource: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars2.toIterator)

  "Generate a National Aggregate 11-6 report" in {
    N11_6.generate(nationalSource, fips).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "11-6"
        fipsCode mustBe "nationwide"
        report.parseJson.asJsObject.getFields("table", "type", "description") match {
          case Seq(JsString(table), JsString(reportType), JsString(desc)) =>
            table mustBe "11-6"
            reportType mustBe "National Aggregate"
            desc mustBe descriptionN116
        }
    }
  }
}
