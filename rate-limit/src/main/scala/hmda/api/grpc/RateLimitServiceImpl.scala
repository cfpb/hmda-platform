package hmda.rateLimit.api.grpc

import scala.concurrent.Future
import pb.lyft.ratelimit.{RateLimitService, RateLimitRequest, RateLimitResponse}
import pb.lyft.ratelimit.RateLimitResponse.Code._
import scala.concurrent.Future
import org.slf4j.LoggerFactory

class RateLimitServiceImpl extends RateLimitService {

  val log = LoggerFactory.getLogger("hmda")

  override def shouldRateLimit(
      in: RateLimitRequest): Future[RateLimitResponse] = {
    log.info("endpoint hit")
    Future.successful(RateLimitResponse(OVER_LIMIT))
  }

}
