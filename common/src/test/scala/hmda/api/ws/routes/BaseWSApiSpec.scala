package hmda.api.ws.routes

import akka.http.scaladsl.model.headers.{ HttpOrigin, Origin }
import akka.http.scaladsl.testkit.{ ScalatestRouteTest, WSProbe }
import hmda.api.http.model.HmdaServiceStatus
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser._
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.ExecutionContext

class BaseWSApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest {

  val ec: ExecutionContext = system.dispatcher

  val wsClient = WSProbe()

  "Websockets API Service" must {
    "Return status" in {
      WS("/", wsClient.flow) ~> Origin(HttpOrigin("http://ffiec.cfpb.gov")) ~> BaseWsApi.route("hmda-ws-api") ~> check {
        isWebSocketUpgrade mustEqual true
        wsClient.sendMessage("status")
        val messageJson: Json =
          parse(wsClient.expectMessage().asTextMessage.getStrictText).toOption.get

        val hmdaStatus = messageJson
          .as[HmdaServiceStatus]
          .getOrElse(HmdaServiceStatus("", "", "", "",""))
        hmdaStatus.status mustBe "OK"
      }
    }
  }
}