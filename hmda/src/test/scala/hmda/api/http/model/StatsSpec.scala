package hmda.api.http.model

import org.scalatest.{MustMatchers, WordSpec}
import hmda.api.http.model.{ SignedLeiCountResponse, SignedLeiListResponse, TotalLeiCountResponse, TotalLeiListResponse }

class StatsSpec extends WordSpec with MustMatchers {
  "response should have correct count" in {
    val response = SignedLeiCountResponse(3)
    assert(SignedLeiCountResponse.apply(3) == response)
  }
}