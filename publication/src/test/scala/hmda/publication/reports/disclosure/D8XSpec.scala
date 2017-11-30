package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json.{ JsArray, JsString }

import scala.concurrent.Future

class D8XSpec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val respId = "98765"
  val fips = 13420 // Bemidji, MN
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(propertyType = 3)
    lar.copy(respondentId = respId, geography = geo, loan = loan)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionD85 = "Reasons for denial of applications for loans on dwellings for 5 or more families, by race, ethnicity, gender and income of applicant"

  "Generate a Disclosure 8-5 report" in {
    D8X.generate(D85, source, fips, respId, Future("Bemidji Test Bank")).map { result =>
      result.asJsObject.getFields("respondentId", "institutionName", "table", "description", "msa") match {
        case Seq(JsString(respondentId), JsString(instName), JsString(table), JsString(desc), msa) =>
          respondentId mustBe respId
          instName mustBe "Bemidji Test Bank"
          table mustBe "8-5"
          desc mustBe descriptionD85
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Bemidji, MN"
          }
      }
    }
  }

  "Include correct applicant Characteristics" in {
    D8X.generate(D85, source, fips, respId, Future("Bemidji Test Bank")).map { result =>
      result.asJsObject.getFields("applicantCharacteristics") match {

        case Seq(JsArray(characteristics)) =>
          characteristics must have size 5
          characteristics.head.asJsObject.getFields("characteristic", "races") match {

            case Seq(JsString(char), JsArray(races)) =>
              char mustBe "race"
              races must have size 8
              races.head.asJsObject.getFields("race", "denialReasons") match {

                case Seq(JsString(race), JsArray(reasons)) =>
                  race mustBe "American Indian/Alaska Native"
                  reasons must have size 10
              }
          }
      }
    }
  }

}
