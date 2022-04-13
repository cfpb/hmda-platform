package hmda.uli.api.grpc

import akka.actor.ActorSystem
import akka.stream.Materializer
import hmda.grpc.services.{ ValidUliRequest, ValidUliResponse }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Span
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

import scala.concurrent.Await
import scala.concurrent.duration._

class CheckDigitServiceImplSpec extends WordSpec with MustMatchers with BeforeAndAfterAll with ScalaFutures {

  implicit val patience =
    PatienceConfig(5.seconds, Span(100, org.scalatest.time.Millis))

  val system       = ActorSystem("CheckDigitActorSystem")
  implicit val mat = Materializer(system)
  val service      = new CheckDigitServiceImpl(mat)

  override def afterAll(): Unit =
    Await.ready(system.terminate(), 5.seconds)

  "CheckDigitServiceImpl" must {
    "check valid ULI" in {
      val reply =
        service.validateUli(ValidUliRequest("10Cx939c5543TqA1144M999143X10"))
      reply.futureValue mustBe ValidUliResponse(true)
    }
    "check invalid ULI" in {
      val reply =
        service.validateUli(ValidUliRequest("10Cx939c5543TqA1144M999143X11"))
      reply.futureValue mustBe ValidUliResponse(false)
    }
  }

}