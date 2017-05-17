package model

import hmda.census.model.RssdToMsa
import org.scalatest.{ MustMatchers, WordSpec }

class RssdToMsaSpec extends WordSpec with MustMatchers {
  "RssdToMsa" must {
    "map values correctly" in {
      val map = RssdToMsa.map
      map.keys.toList.length mustBe 123703
    }
  }
}
