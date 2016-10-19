package hmda.parser.fi

import org.scalacheck.Gen
import scalajs.js

object DateGenerators {
  def randomDate: Gen[Int] = {
    val beginDate = new js.Date("2017-01-01")
    val endDate = new js.Date("2020-12-31")

    for {
      randomDate <- Gen.choose(beginDate.getTime(), endDate.getTime())
    } yield new js.Date(randomDate).getTime().toInt
  }
}
