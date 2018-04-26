package hmda.api.http.public

import akka.actor.ActorRef
import akka.pattern.ask
import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.CreateHmdaFiler
import hmda.model.institution.FilingGenerators._
import hmda.persistence.HmdaSupervisor
import hmda.persistence.HmdaSupervisor.{ FindHmdaFilerPersistence, FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.{ HmdaFilerPersistence, SubmissionPersistence }
import hmda.validation.stats.{ SubmissionLarStats, ValidationStats }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.model.public.HmdaFilerResponse
import hmda.census.model.Msa
import hmda.model.fi.{ Signed, Submission, SubmissionId }
import hmda.model.institution.HmdaFiler
import hmda.persistence.institutions.SubmissionPersistence.{ CreateSubmission, UpdateSubmissionStatus }
import hmda.persistence.model.MsaGenerators
import hmda.persistence.processing.SubmissionManager
import hmda.persistence.processing.SubmissionManager.GetActorRef
import hmda.validation.stats.ValidationStats.AddIrsStats

import spray.json._

class HmdaFilersPathSpec extends WordSpec with MustMatchers with BeforeAndAfterAll with ScalatestRouteTest with HmdaFilerPaths with MsaGenerators {

  val duration = 10.seconds

  override implicit val timeout: Timeout = Timeout(duration)
  override val ec: ExecutionContext = system.dispatcher
  override val log: LoggingAdapter = NoLogging

  val hmdaFiler1 = hmdaFilerGen.sample.getOrElse(HmdaFiler("inst1", "resp1", "2017", "Bank 1")).copy(period = "2017")
  val hmdaFiler2 = hmdaFilerGen.sample.getOrElse(HmdaFiler("inst2", "resp2", "2016", "Bank2")).copy(period = "2016")

  val subId = SubmissionId(hmdaFiler1.institutionId, "2017", 1)

  val list = listOfMsaGen.sample.getOrElse(List[Msa]()) :+ Msa("13980", "Blacksburg-Christiansburg-Radford, VA")

  val validationStats = system.actorOf(ValidationStats.props())
  val supervisor = system.actorOf(HmdaSupervisor.props(validationStats))

  val fHmdaFilerActor = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name)).mapTo[ActorRef]
  val fSubmissions = (supervisor ? FindSubmissions(SubmissionPersistence.name, subId.institutionId, subId.period)).mapTo[ActorRef]

  override def beforeAll(): Unit = {
    super.beforeAll()
    val hmdaFilerPersistence = Await.result(fHmdaFilerActor, duration)
    val submissionPersistence = Await.result(fSubmissions, duration)

    hmdaFilerPersistence ! CreateHmdaFiler(hmdaFiler1)
    hmdaFilerPersistence ! CreateHmdaFiler(hmdaFiler2)

    val submissionSetup = for {
      sub <- (submissionPersistence ? CreateSubmission).mapTo[Option[Submission]]
      sign <- (submissionPersistence ? UpdateSubmissionStatus(sub.getOrElse(Submission(SubmissionId("0"))).id, Signed)).mapTo[Option[Submission]]
    } yield sign
    Await.result(submissionSetup, duration)

    val larStatsF = for {
      manager <- (supervisor ? FindProcessingActor(SubmissionManager.name, subId)).mapTo[ActorRef]
      larStats <- (manager ? GetActorRef(SubmissionLarStats.name)).mapTo[ActorRef]
    } yield larStats
    val larStats = Await.result(larStatsF, 5.seconds)
    larStats ! AddIrsStats(list, subId)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "HMDA Filer" must {
    "retrieve list of filers" in {
      Get("/filers") ~> hmdaFilersPath(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[HmdaFilerResponse] mustBe HmdaFilerResponse(Set(hmdaFiler1, hmdaFiler2))
      }
    }
    "filter filers by period" in {
      Get("/filers/2017") ~> hmdaFilersPath(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[HmdaFilerResponse] mustBe HmdaFilerResponse(Set(hmdaFiler1))
      }
    }
    "find msa/mds by period and institution" in {
      Get(s"/filers/2017/${hmdaFiler1.institutionId}/msaMds") ~> hmdaFilerMsasPath(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[String].parseJson.asJsObject.getFields("year", "institution", "msaMds") match {
          case Seq(JsString(year), JsObject(institution), JsArray(msas)) =>
            year mustBe "2017"
            institution.getOrElse("name", "") mustBe JsString(hmdaFiler1.name)
            msas.toList.length mustBe list.length
        }
      }
    }
  }

}
