package hmda.parser.fi

import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

  def dateGen: Gen[String] = {
    for {
      year <- Gen.choose(2017, 2020) // or could parameterize if needed
      month <- Gen.choose(1, 12)
      day <- Gen.choose(1, 28) // NOTE: this misses some dates! if that's a problem, we can do something more complex.
    } yield LocalDate.of(year, month, day).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
  }
}
