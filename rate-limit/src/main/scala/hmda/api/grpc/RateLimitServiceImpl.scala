package hmda.rateLimit.api.grpc

import scala.concurrent.Future
import pb.lyft.ratelimit.{RateLimitService, RateLimitRequest, RateLimitResponse}
import pb.lyft.ratelimit.RateLimitResponse.Code._
import scala.concurrent.Future
import org.slf4j.LoggerFactory
import com.google.common.util.concurrent.RateLimiter

class RateLimitServiceImpl(limit: Int) extends RateLimitService {

  val log = LoggerFactory.getLogger("hmda")

  val rateLimiter = RateLimiter.create(limit)

  override def shouldRateLimit(
      in: RateLimitRequest): Future[RateLimitResponse] = {
    log.info(in.toString)
    if (rateLimiter.tryAcquire()) {
      Future.successful(RateLimitResponse(OK))
    } else {
      log.info("limit exceeded")
      Future.successful(RateLimitResponse(OVER_LIMIT))
    }
  }

}
