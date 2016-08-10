package hmda.api.http

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import akka.event.LoggingAdapter

trait HmdaCustomDirectives {
  val log: LoggingAdapter

  def timedGet: Directive0 = get & time
  def timedPost: Directive0 = post & time

  def time: Directive0 = {
    val startTime = System.currentTimeMillis()

    mapResponse { response =>
      val endTime = System.currentTimeMillis()
      val responseTime = endTime - startTime
      log.debug(s"Request took $responseTime ms")
      response
    }

  }

}
