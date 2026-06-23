package hmda.api.http

import org.apache.pekko.http.scaladsl.server.PathMatcher1
import hmda.utils.YearUtils.{ isValidQuarter }
import org.apache.pekko.http.scaladsl.server.Directives._

object PathMatchers {
  val Quarter: PathMatcher1[String] = Segment.flatMap(Option(_).filter(isValidQuarter))
}
