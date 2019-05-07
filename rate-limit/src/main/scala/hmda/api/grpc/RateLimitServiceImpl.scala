package hmda.rateLimit.api.grpc

import akka.stream.Materializer
import hmda.grpc.services.{
  RateLimitService,
  RateLimitRequest,
  RateLimitResponse
}
import hmda.grpc.services.RateLimitResponse.Code._
import hmda.grpc.services._
import scala.concurrent.Future

class RateLimitServiceImpl(materializer: Materializer)
    extends RateLimitService {

  override def shouldRateLimit(
      in: RateLimitRequest): Future[RateLimitResponse] = {
    Future.successful(RateLimitResponse(OVER_LIMIT))
  }

}
