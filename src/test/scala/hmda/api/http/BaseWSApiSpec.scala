package hmda.api.http

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import hmda.api.http.model.common.HmdaServiceStatus
import io.circe.Json
import org.scalatest.{MustMatchers, WordSpec}
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.ExecutionContext

class BaseWSApiSpec
    extends WordSpec
    with MustMatchers
    with ScalatestRouteTest
    with BaseWsApi {

  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher

  val wsClient = WSProbe()

  "Websockets API Service" must {
    "Return status" in {
      WS("/", wsClient.flow) ~> routes("hmda-ws-api") ~> check {
        isWebSocketUpgrade mustEqual true
        wsClient.sendMessage("status")
        val messageJson: Json =
          parse(wsClient.expectMessage().asTextMessage.getStrictText).toOption.get

        val hmdaStatus = messageJson
          .as[HmdaServiceStatus]
          .getOrElse(HmdaServiceStatus("", "", "", ""))
        hmdaStatus.status mustBe "OK"
      }
    }
  }
}
