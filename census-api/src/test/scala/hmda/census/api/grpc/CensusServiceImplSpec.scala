package hmda.census.api.grpc

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.census.records.CensusRecords
import hmda.grpc.services._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Span
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

class CensusServiceImplSpec
    extends WordSpec
    with MustMatchers
    with BeforeAndAfterAll
    with ScalaFutures
    with CensusRecords {

  implicit val patience =
    PatienceConfig(5.seconds, Span(100, org.scalatest.time.Millis))

  val system = ActorSystem("CensusService")
  implicit val mat = ActorMaterializer.create(system)

  val service =
    new CensusServiceImpl(mat, indexedTract, indexedCounty, indexedSmallCounty)

  override def afterAll(): Unit = {
    Await.ready(system.terminate(), 5.seconds)
  }

  "CensusServiceImpl" must {
    "check valid Tract" in {
      val reply =
        service.validateTract(ValidTractRequest("35003976400"))
      reply.futureValue mustBe ValidTractResponse(true)
    }
    "check invalid Tract" in {
      val reply =
        service.validateTract(ValidTractRequest("35003976401"))
      reply.futureValue mustBe ValidTractResponse(false)
    }

    "check valid County" in {
      val reply =
        service.validateCounty(ValidCountyRequest("35003"))
      reply.futureValue mustBe ValidCountyResponse(true)
    }
    "check invalid County" in {
      val reply =
        service.validateCounty(ValidCountyRequest("00001"))
      reply.futureValue mustBe ValidCountyResponse(false)
    }

    "check large County" in {
      val reply =
        service.validatePopulation(ValidPopulationRequest("00000"))
      reply.futureValue mustBe ValidPopulationResponse(false)
    }

  }

}
