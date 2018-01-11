package hmda.http.common.directives

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._

trait HmdaTimeDirectives {

  val log: LoggingAdapter

  def timedGet = get & time & extractUri
  def timedPost = post & time & extractUri
  def timedPut = post & time & extractUri

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
