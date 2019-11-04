package hmda.api.http

import akka.http.scaladsl.server.PathMatcher1
import hmda.utils.YearUtils.{ isValidQuarter, isValidYear }
import akka.http.scaladsl.server.Directives._

object PathMatchers {
  val Quarter: PathMatcher1[String] = Segment.flatMap(Option(_).filter(isValidQuarter))
  val Year: PathMatcher1[Int]       = IntNumber.flatMap(Option(_).filter(isValidYear))
}
