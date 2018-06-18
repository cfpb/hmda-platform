package hmda.model.filing

import hmda.model.census.Census.states
import hmda.model.institution.Agency
import org.scalacheck.Gen

object FilingGenerators {

  implicit def agencyCodeGen: Gen[Int] = {
    Gen.oneOf(Agency.values.filter(x => x != -1))
  }

  implicit def agencyGen: Gen[Agency] = {
    for {
      agencyCode <- agencyCodeGen
      agency = Agency.valueOf(agencyCode)
    } yield agency
  }

  implicit def leiGen: Gen[String] = {
    stringOfN(20, Gen.alphaNumChar)
  }

  def stringOfN(n: Int, genChar: Gen[Char]): Gen[String] = {
    Gen.listOfN(n, genChar).map(_.mkString)
  }

  def stringOfUpToN(n: Int, genChar: Gen[Char]): Gen[String] = {
    val stringGen = Gen.listOf(genChar).map(_.mkString)
    Gen.resize(n, stringGen)
  }

  def stringOfOneToN(n: Int, genChar: Gen[Char]): Gen[String] = {
    val stringGen = Gen.nonEmptyListOf(genChar).map(_.mkString)
    Gen.resize(n, stringGen)
  }

  implicit def emailListGen: Gen[List[String]] = {
    Gen.listOf(emailGen)
  }

  implicit def emailGen: Gen[String] = {
    for {
      name <- Gen.alphaStr.filter(s => s.nonEmpty)
      at = "@"
      domain <- Gen.alphaStr.filter(s => s.nonEmpty)
      dotCom = ".com"
    } yield List(name, at, domain, dotCom).mkString
  }

  implicit def activityYearGen: Gen[Int] = {
    Gen.oneOf(2018, 2019)
  }

  implicit def stateGen: Gen[String] = {
    Gen.oneOf(states.keys.toList)
  }

}
