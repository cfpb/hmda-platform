package hmda.rateLimit.api.grpc

import akka.stream.Materializer
import scala.concurrent.Future
import hmda.grpc.services.{
  RateLimitService,
  RateLimitRequest,
  RateLimitResponse
}
import hmda.grpc.services.RateLimitResponse.Code._
import hmda.grpc.services._
import scala.concurrent.Future
import org.slf4j.LoggerFactory

class RateLimitServiceImpl(materializer: Materializer)
    extends RateLimitService {

  val log = LoggerFactory.getLogger("hmda")

  override def shouldRateLimit(
      in: RateLimitRequest): Future[RateLimitResponse] = {
    log.info("endpoint hit")
    Future.successful(RateLimitResponse(OVER_LIMIT))
  }

}
