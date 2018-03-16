package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.institution.InstitutionGenerators.sampleInstitution
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json.{ JsArray, JsString, _ }

import scala.concurrent.Future

class AISpec
    extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val instSet = Set(sampleInstitution, sampleInstitution)

  val fips = 18700 //Corvallis, OR
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    lar.copy(geography = geo, respondentId = instSet.head.respondentId)
  }

  val aggregateSource: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionAI = "List of financial institutions whose data make up the 2017 MSA/MD aggregate report"

  "Generate an Aggregate 4-2 report" in {
    AI.generateWithInst(aggregateSource, fips, Future(instSet)).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "AI"
        fipsCode mustBe fips.toString
        report.parseJson.asJsObject.getFields("table", "type", "description", "year", "msa") match {
          case Seq(JsString(table), JsString(reportType), JsString(desc), JsString(reportYear), msa) =>
            table mustBe "i"
            reportType mustBe "Aggregate"
            desc mustBe descriptionAI
            msa.asJsObject.getFields("name") match {
              case Seq(JsString(msaName)) => msaName mustBe "Corvallis, OR"
            }
        }
    }
  }

  "Include correct institution names" in {
    AI.generateWithInst(aggregateSource, fips, Future(instSet)).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "AI"
        fipsCode mustBe fips.toString
        report.parseJson.asJsObject.getFields("institutions") match {
          case Seq(JsArray(inst)) =>
            inst.head mustBe JsString(instSet.head.respondent.name)
            inst.size mustBe 1
        }
    }
  }
}
