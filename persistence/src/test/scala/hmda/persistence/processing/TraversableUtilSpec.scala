package hmda.persistence.processing

import org.scalatest.{ MustMatchers, WordSpec }

class TraversableUtilSpec extends WordSpec with MustMatchers {
  import hmda.persistence.processing.TraversableUtil._

  "TraversableUtils" must {
    "count iterations starting at 2" in {
      val test = List(0, 0, 0, 0, 0).map(doIndexed((i, x) => x + i))
      test mustBe List(2, 3, 4, 5, 6)
    }
  }

}

