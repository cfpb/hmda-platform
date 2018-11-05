package hmda.model.filing.submissions

import org.scalatest.{MustMatchers, WordSpec}

class PaginatedResourceSpec extends WordSpec with MustMatchers {

  val resource = PaginatedResource(totalRecords = 44, offset = 3)(_)
  val resource2 = PaginatedResource(totalRecords = 44, offset = 0)(_)

  "Paginated Resource paginates offset collections" must {
    "first page contains index 0 thru (20 - offset)" in {
      val pageOne = resource(1)

      pageOne.fromIndex mustBe 0
      pageOne.toIndex mustBe 17
    }

    "second page contains next 20 results" in {
      val pageTwo = resource(2)

      pageTwo.fromIndex mustBe 17
      pageTwo.toIndex mustBe 37
    }

    "third page contains all remaining" in {
      val pageThree = resource(3)

      pageThree.fromIndex mustBe 37
      pageThree.toIndex mustBe 44
    }
  }

  "Paginated Resource paginates collections with no offset" must {
    "first page contains index 0 thru 20" in {
      val pageOne = resource2(1)

      pageOne.fromIndex mustBe 0
      pageOne.toIndex mustBe 20
    }

    "second page contains next 20 results" in {
      val pageTwo = resource2(2)

      pageTwo.fromIndex mustBe 20
      pageTwo.toIndex mustBe 40
    }

    "third page contains all remaining" in {
      val pageThree = resource2(3)

      pageThree.fromIndex mustBe 40
      pageThree.toIndex mustBe 44
    }
  }

}
