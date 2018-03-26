package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators.sampleInstitution
import hmda.persistence.HmdaSupervisor
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.messages.commands.institutions.InstitutionCommands.CreateInstitution
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.validation.stats.ValidationStats
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json.{ JsArray, JsString, _ }

import scala.concurrent.duration._
import scala.concurrent.Future

class AISpec
    extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {
  /*

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(10.seconds)

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val institution = sampleInstitution

  val fips = 18700 //Corvallis, OR
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    lar.copy(geography = geo, respondentId = institution.respondentId)
  }

  val aggregateSource: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val descriptionAI = "List of financial institutions whose data make up the 2017 MSA/MD aggregate report"

  "Set up: create supervisor and institutionsPersistence" in {
    val validationStats = ValidationStats.createValidationStats(system)
    val supervisor = HmdaSupervisor.createSupervisor(system, validationStats)
    val institutionsActorF: Future[ActorRef] = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
    for {
      inst <- institutionsActorF
      created <- inst ? CreateInstitution(institution)
      state <- (inst ? GetState).mapTo[Set[Institution]]
    } yield {
      state.size mustBe 1
    }
  }

  "Generate an Aggregate 4-2 report" in {
    AI.generate(aggregateSource, fips).map {
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
    AI.generate(aggregateSource, fips).map {
      case AggregateReportPayload(reportId, fipsCode, report) =>
        reportId mustBe "AI"
        fipsCode mustBe fips.toString
        report.parseJson.asJsObject.getFields("institutions") match {
          case Seq(JsArray(ins)) =>
            ins.head mustBe JsString(institution.respondent.name)
            ins.size mustBe 1
        }
    }
  }
  */
}
