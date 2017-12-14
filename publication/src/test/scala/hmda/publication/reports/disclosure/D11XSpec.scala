package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json.{ JsArray, JsString }

import scala.concurrent.Future

class D11XSpec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val respId = "54345"
  val fips = 11540 // Appleton, WI

  val lars = Gen.listOfN(100, larWithValidGeoGen).sample.get.map { lar: LoanApplicationRegister =>
    val loan = lar.loan.copy(loanType = 2, purpose = 3, propertyType = 1, occupancy = 1)
    lar.copy(respondentId = respId, loan = loan, lienStatus = 1)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionD85 = "Pricing information for FHA refinancing loans, first lien, 1- to 4-family owner-occupied dwelling (excludes manufactured homes), by borrower or census tract characteristics"

  "Generate a Disclosure 11-5 report" in {
    D11X.generate(D11_5, source, fips, respId, Future("Fox Valley Test Bank")).map { result =>
      result.asJsObject.getFields("respondentId", "institutionName", "table", "description", "msa") match {
        case Seq(JsString(respondentId), JsString(instName), JsString(table), JsString(desc), msa) =>
          respondentId mustBe respId
          instName mustBe "Fox Valley Test Bank"
          table mustBe "11-5"
          desc mustBe descriptionD85
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Appleton, WI"
          }
      }
    }
  }

  "Include correct borrower Characteristics" in {
    D11X.generate(D11_5, source, fips, respId, Future("Fox Valley Test Bank")).map { result =>
      result.asJsObject.getFields("borrowerCharacteristics") match {

        case Seq(JsArray(characteristics)) =>
          characteristics must have size 5
          characteristics.head.asJsObject.getFields("characteristic", "races") match {

            case Seq(JsString(char), JsArray(races)) =>
              char mustBe "Race"
              races must have size 8
              races.head.asJsObject.getFields("race", "pricingInformation") match {

                case Seq(JsString(race), JsArray(pricing)) =>
                  race mustBe "American Indian/Alaska Native"
                  pricing must have size 9
              }
          }
      }
    }
  }

  "Include correct Census Tract Characteristics" in {
    D11X.generate(D11_5, source, fips, respId, Future("Fox Valley Test Bank")).map { result =>
      result.asJsObject.getFields("censusTractCharacteristics") match {

        case Seq(JsArray(characteristics)) =>
          characteristics must have size 2
          characteristics.head.asJsObject.getFields("characteristic", "compositions") match {

            case Seq(JsString(char), JsArray(races)) =>
              char mustBe "Racial/Ethnic Composition"
              races must have size 5
              races.head.asJsObject.getFields("composition", "pricingInformation") match {

                case Seq(JsString(race), JsArray(pricing)) =>
                  race mustBe "Less than 10% minority"
                  pricing must have size 9
              }
          }
      }
    }
  }
}
