package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json._

class A1Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val fips = 11540 // Appleton, WI
  val lars = Gen.listOfN(100, larWithValidGeoGen).sample.get

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionA1 = "Disposition of loan applications, by location of property and type of loan"

  "Generate an Aggregate 1 report" in {
    A1.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("table", "description", "msa") match {
        case Seq(JsString(table), JsString(desc), msa) =>
          table mustBe "1"
          desc mustBe descriptionA1
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Appleton, WI"
          }
      }
    }
  }

  "Include correct structure" in {
    A1.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("tracts") match {

        case Seq(JsArray(characteristics)) =>
          characteristics.head.asJsObject.getFields("tract", "dispositions") match {

            case Seq(JsString(char), JsArray(comps)) =>
              char.split("/").size mustBe 3
              comps must have size 7
              comps.head.asJsObject.getFields("title", "values") match {

                case Seq(JsString(comp), JsArray(disp)) =>
                  comp mustBe "Loans originated"
                  disp must have size 7
              }
          }
      }
    }
  }
}

