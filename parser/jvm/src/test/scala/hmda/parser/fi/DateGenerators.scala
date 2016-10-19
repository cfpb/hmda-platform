package hmda.parser.fi

import java.text.SimpleDateFormat
import java.util.Date

import org.scalacheck.Gen

object DateGenerators {
  def randomDate: Gen[Int] = {
    val dateFormat = new SimpleDateFormat("yyyyMMdd")

    val beginDate = dateFormat.parse("20170101")
    val endDate = dateFormat.parse("20201231")
    for {
      randomDate <- Gen.choose(beginDate.getTime, endDate.getTime)
    } yield dateFormat.format(new Date(randomDate)).toInt
  }
}
