package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json._

class A31Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val fips = 11540 // Appleton, WI
  val lars = Gen.listOfN(100, larWithValidGeoGen).sample.get.map { lar: LoanApplicationRegister =>
    val loan = lar.loan.copy(loanType = 2, purpose = 3, propertyType = 1, occupancy = 1)
    lar.copy(loan = loan, lienStatus = 1)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionA31 = "Loans sold, by characteristics of borrower and census tract in which property is located and by type of purchaser (includes originations and purchased loans)"

  "Generate an Aggregate 3-1 report" in {
    A31.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("table", "description", "msa") match {
        case Seq(JsString(table), JsString(desc), msa) =>
          table mustBe "3-1"
          desc mustBe descriptionA31
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Appleton, WI"
          }
      }
    }
  }

  "Include correct borrowerCharacteristics json" in {
    A31.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("borrowerCharacteristics") match {

        case Seq(JsArray(characteristics)) =>
          characteristics must have size 4
          characteristics.head.asJsObject.getFields("characteristic", "races") match {

            case Seq(JsString(char), JsArray(races)) =>
              char mustBe "Race"
              races must have size 8
              races.head.asJsObject.getFields("race", "purchasers") match {

                case Seq(JsString(race), JsArray(disp)) =>
                  race mustBe "American Indian/Alaska Native"
                  disp must have size 9
              }
          }
      }
    }
  }

  "Include correct censusCharacteristics json" in {
    N31.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("censusCharacteristics") match {

        case Seq(JsArray(characteristics)) =>
          characteristics must have size 2
          characteristics.head.asJsObject.getFields("characteristic", "tractPctMinorities") match {

            case Seq(JsString(char), JsArray(minorityPct)) =>
              char mustBe "Racial/Ethnic Composition"
              minorityPct must have size 5
              minorityPct.head.asJsObject.getFields("tractPctMinority", "purchasers") match {

                case Seq(JsString(tractMinorityPct), JsArray(disp)) =>
                  tractMinorityPct mustBe "Less than 10% minority"
                  disp must have size 9
              }
          }
      }
    }
  }

  "Include correct totals json" in {
    A31.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("total") match {
        case Seq(totals) =>
          totals.asJsObject.getFields("purchasers") match {
            case Seq(JsArray(dispositions)) =>
              dispositions must have size 9
              dispositions.head.asJsObject.getFields("disposition", "count", "value") match {
                case Seq(JsString(name), JsNumber(count), JsNumber(value)) =>
                  name mustBe "Fannie Mae"
                  count <= 100 && count >= 0 mustBe true
                  value >= 0 mustBe true
              }
          }
      }
    }
  }

}
