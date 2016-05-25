package hmda.parser.fi

import java.text.SimpleDateFormat
import java.util.Date

import org.scalacheck.Gen

trait FIGenerators {

  implicit def agencyCodeGen: Gen[Int] = {
    Gen.oneOf(1, 2, 3, 5, 7, 9)
  }

  implicit def respIdGen: Gen[String] = {
    Gen.alphaStr
  }

  // utility functions

  def stringOfN(n: Int, genOne: Gen[Char]): Gen[String] = {
    Gen.listOfN(n, genOne).map(_.mkString)
  }

  // this name may be too similar to Gen.option. (to be fair, it's not unrelated. just... domain-specific.)
  def optional[T](g: Gen[T], emptyVal: String = ""): Gen[String] = {
    Gen.oneOf(g.map(_.toString), Gen.const(emptyVal))
  }

  // returns Int in yyyyMMdd format. must be a correct calendar date.
  def dateGen: Gen[Int] = {
    val dateFormat = new SimpleDateFormat("yyyyMMdd")

    val beginDate = dateFormat.parse("20170101")
    val endDate = dateFormat.parse("202021231")
    for {
      randomDate <- Gen.choose(beginDate.getTime, endDate.getTime)
    } yield dateFormat.format(new Date(randomDate)).toInt
  }
}
