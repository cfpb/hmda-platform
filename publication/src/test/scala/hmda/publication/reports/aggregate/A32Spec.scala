package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json._

class A32Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val fips = 11540 // Appleton, WI
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    lar.copy(actionTakenType = 1, lienStatus = 1)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionA32 = "Pricing Information for First and Junior Lien Loans Sold by Type of Purchaser (includes originations only)"

  "Generate a Disclosure 3-2 report" in {
    A32.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("respondentId", "institutionName", "table", "description", "msa") match {
        case Seq(JsString(table), JsString(desc), msa) =>
          table mustBe "3-2"
          desc mustBe descriptionA32
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Appleton, WI"
          }
      }
    }
  }

  "Include correct pricingInformation json" in {
    A32.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("pricingInformation") match {

        case Seq(JsArray(categories)) =>
          categories must have size 2
          categories.head.asJsObject.getFields("pricing", "purchasers") match {

            case Seq(JsString(pricing), JsArray(purchasers)) =>
              pricing mustBe "No reported pricing data"
              purchasers must have size 9
              purchasers.head.asJsObject.getFields("disposition", "firstLienCount", "firstLienValue", "juniorLienCount", "juniorLienValue") match {

                case Seq(JsString(disp), JsNumber(flc), JsNumber(flv), JsNumber(jlc), JsNumber(jlv)) =>
                  disp mustBe "Fannie Mae"
                  flc >= 0 && flc <= 100 mustBe true
              }
          }
      }
    }
  }

  "Include correct points json" in {
    N32.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("points") match {

        case Seq(JsArray(categories)) =>
          categories must have size 10
          categories.head.asJsObject.getFields("pricing", "purchasers") match {

            case Seq(JsString(pricing), JsArray(purchasers)) =>
              pricing mustBe "1.50 - 1.99"
              purchasers must have size 9
              purchasers.head.asJsObject.getFields("disposition", "firstLienCount", "firstLienValue", "juniorLienCount", "juniorLienValue") match {

                case Seq(JsString(disp), JsNumber(flc), JsNumber(flv), JsNumber(jlc), JsNumber(jlv)) =>
                  disp mustBe "Fannie Mae"
                  flc >= 0 && flc <= 100 mustBe true
              }
          }
      }
    }
  }

  "Include correct hoepa json" in {
    N32.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("hoepa") match {

        case Seq(hoepa) =>
          hoepa.asJsObject.getFields("pricing", "purchasers") match {

            case Seq(JsString(pricing), JsArray(purchasers)) =>
              pricing mustBe "HOEPA loans"
              purchasers must have size 9
          }
      }
    }
  }

}
