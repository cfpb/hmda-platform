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

class D4XSpec extends AsyncWordSpec with MustMatchers
    with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  def propType = Gen.oneOf(1, 2).sample.get

  val respId = "10101"
  val fips = 24300 // Grand Junction, CO
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(loanType = 1, propertyType = propType, purpose = 1)
    lar.copy(respondentId = respId, geography = geo, loan = loan)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val description = "Disposition of applications for conventional home-purchase loans 1- to 4- family and manufactured home dwellings, by race, ethnicity, gender and income of applicant"

  "Generate a Disclosure 4-2 report" in {
    D4X.generate(D42, source, fips, respId, Future("Grand Junction Mortgage Co.")).map { result =>
      result.asJsObject.getFields("table", "description", "msa", "respondentId", "institutionName") match {
        case Seq(JsString(table), JsString(desc), msa, JsString(resp), JsString(instName)) =>
          table mustBe "4-2"
          desc mustBe description
          resp mustBe "10101"
          instName mustBe "Grand Junction Mortgage Co."
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Grand Junction, CO"
          }
      }
    }
  }

  "Include correct demographics for dispositions" in {
    D4X.generate(D42, source, fips, respId, Future("Grand Junction Mortgage Co.")).map { result =>
      result.asJsObject.getFields("races", "minorityStatuses", "ethnicities", "incomes", "total") match {
        case Seq(JsArray(races), JsArray(ms), JsArray(ethnicities), JsArray(incomes), JsArray(total)) =>
          races must have size 8
          ms must have size 2
          ethnicities must have size 4
          incomes must have size 6
          total must have size D4X.dispositions.size
          races.head.asJsObject.getFields("race", "dispositions", "genders") match {
            case Seq(JsString(name), JsArray(disp), JsArray(genders)) =>
              name mustBe "American Indian/Alaska Native"
              disp must have size 6
              genders must have size 3
          }
      }
    }
  }

}
