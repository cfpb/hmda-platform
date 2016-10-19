package hmda.parser.fi

import hmda.model.institution.Agency
import org.scalacheck.Gen

trait FIGenerators {

  implicit def agencyCodeGen: Gen[Int] = {
    Gen.oneOf(Agency.values).map(_.value)
  }

  implicit def respIdGen: Gen[String] = {
    stringOfOneToN(10, Gen.alphaChar)
  }

  // utility functions

  def stringOfN(n: Int, genOne: Gen[Char]): Gen[String] = {
    Gen.listOfN(n, genOne).map(_.mkString)
  }

  def stringOfUpToN(n: Int, genOne: Gen[Char]): Gen[String] = {
    val stringGen = Gen.listOf(genOne).map(_.mkString)
    Gen.resize(n, stringGen)
  }

  def stringOfOneToN(n: Int, genOne: Gen[Char]): Gen[String] = {
    val stringGen = Gen.nonEmptyListOf(genOne).map(_.mkString)
    Gen.resize(n, stringGen)
  }

  // this name may be too similar to Gen.option. (to be fair, it's not unrelated. just... domain-specific.)
  def optional[T](g: Gen[T], emptyVal: String = ""): Gen[String] = {
    Gen.oneOf(g.map(_.toString), Gen.const(emptyVal))
  }

  // returns Int in yyyyMMdd format. must be a correct calendar date.
  // DateGenerators is platform specific, see jvm/src/scala/test/DateGenerators.scala and js/src/scala/test/DateGenerators.scala
  def dateGen: Gen[Int] = DateGenerators.randomDate
}
