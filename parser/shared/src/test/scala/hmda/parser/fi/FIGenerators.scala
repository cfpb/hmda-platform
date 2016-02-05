package hmda.parser.fi

import org.scalacheck.Gen

trait FIGenerators {

  implicit def agencyCodeGen: Gen[Int] = {
    Gen.oneOf(1, 2, 3, 5, 7, 9)
  }

  implicit def respIdGen: Gen[String] = {
    Gen.alphaStr
  }

}
