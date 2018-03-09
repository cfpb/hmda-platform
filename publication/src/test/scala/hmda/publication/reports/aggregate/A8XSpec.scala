package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json._

class A8XSpec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

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
    lar.copy(geography = geo, loan = loan)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionA85 = "Reasons for denial of applications for loans on dwellings for 5 or more families, by race, ethnicity, gender and income of applicant"

  "Generate an Aggregate 8-5 report" in {
    A85.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("table", "description") match {
        case Seq(JsString(table), JsString(desc)) =>
          table mustBe "8-5"
          desc mustBe descriptionA85
      }
    }
  }

  "Include correct applicant Characteristics" in {
    A85.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("applicantCharacteristics") match {

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

  "Generate a National Aggregate 8-5 report" in {
    N85.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("table", "description") match {
        case Seq(JsString(table), JsString(desc)) =>
          table mustBe "8-5"
          desc mustBe descriptionA85
      }
    }
  }

}
