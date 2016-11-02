package hmda.parser.fi

import org.scalacheck.Gen
import scalajs.js

object DateGenerators {
  def randomDate: Gen[Int] = {
    val beginDate = new js.Date("2017-01-01")
    val endDate = new js.Date("2020-12-31")

    for {
      randomDate <- Gen.choose(beginDate.getTime(), endDate.getTime())
    } yield format(randomDate)
  }

  private def format(time: Double): Int = {
    val date = new js.Date(time)
    date.toISOString().slice(0, 10).replace("-", "").toInt
  }
}
