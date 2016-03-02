package hmda.parser.fi

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

  // returns Int in yyyyMMdd format. this feels a bit primitive-obsessed, but is probably OK for now.
  def dateGen: Gen[Int] = {
    for {
      year <- Gen.choose(2017, 2020) // or could parameterize if needed
      month <- Gen.choose(1, 12)
      day <- Gen.choose(1, 31) // now featuring lesser-known dates such as February 31! (OK in the current context)
    } yield year * 10000 + month * 100 + day
  }
}
