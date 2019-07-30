package hmda.rateLimit.api.grpc

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}

import pb.lyft.ratelimit.{RateLimitRequest, RateLimitResponse}
import pb.lyft.ratelimit.RateLimitResponse.Code._
import pb.lyft.ratelimit._

class RateLimitServiceImplTest
    extends WordSpec
    with MustMatchers
    with ScalaFutures {

  val service = new RateLimitServiceImpl(2)
  val request = RateLimitRequest()
  "RateLimitServiceImpl" must {
    "return over limit" in {
      val reply = service.shouldRateLimit(request)
      reply.futureValue mustBe RateLimitResponse(OK)
    }
  }

}
