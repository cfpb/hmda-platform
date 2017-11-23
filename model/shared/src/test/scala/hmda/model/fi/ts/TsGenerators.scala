package hmda.model.fi.ts

import hmda.model.fi.FIGenerators
import hmda.model.util.FITestData.states
import org.scalacheck.Gen

trait TsGenerators extends FIGenerators {

  implicit def ts100ListGen: Gen[List[TransmittalSheet]] = {
    Gen.listOfN(100, tsGen)
  }

  implicit def tsGen: Gen[TransmittalSheet] = {
    for {
      code <- agencyCodeGen
      timeStamp <- timeGen
      activityYear <- activityYearGen
      taxId <- taxIdGen
      totalLines = 10000
      respondent <- respondentGen
      parent <- parentGen
      contact <- contactGen
    } yield TransmittalSheet(
      1,
      code,
      timeStamp,
      activityYear,
      taxId,
      totalLines,
      respondent,
      parent,
      contact
    )
  }

  implicit def timeGen: Gen[Long] = {
    Gen.oneOf(201602021453L, 201602051234L)
  }

  implicit def activityYearGen: Gen[Int] = {
    Gen.oneOf(2017, 2018, 2019, 2020)
  }

  implicit def taxIdGen: Gen[String] = {
    for {
      prefix <- stringOfN(2, Gen.numChar)
      sep = "-"
      suffix <- stringOfN(7, Gen.numChar)
    } yield List(prefix, suffix).mkString(sep)
  }

  implicit def respondentGen: Gen[Respondent] = {
    for {
      id <- respIdGen
      name <- stringOfOneToN(30, Gen.alphaNumChar)
      address <- stringOfOneToN(40, Gen.alphaNumChar)
      city <- stringOfOneToN(25, Gen.alphaNumChar)
      state <- stateGen
      zip <- zipGen
    } yield Respondent(id, name, address, city, state, zip)
  }

  implicit def parentGen: Gen[Parent] = {
    for {
      name <- stringOfUpToN(30, Gen.alphaNumChar)
      address <- stringOfUpToN(40, Gen.alphaNumChar)
      city <- stringOfUpToN(25, Gen.alphaNumChar)
      state <- stateGen
      zip <- zipGen
    } yield Parent(name, address, city, state, zip)
  }

  implicit def stateGen: Gen[String] = {
    Gen.oneOf(states)
  }

  implicit def zipGen: Gen[String] = {
    Gen.oneOf(zip5Gen, zipPlus4Gen)
  }

  implicit def zip5Gen: Gen[String] = stringOfN(5, Gen.numChar)

  implicit def zipPlus4Gen: Gen[String] = {
    for {
      zip <- zip5Gen
      plus <- stringOfN(4, Gen.numChar)
      sep = "-"
    } yield List(zip, plus).mkString(sep)
  }

  implicit def contactGen: Gen[Contact] = {
    for {
      name <- stringOfUpToN(30, Gen.alphaNumChar).filter(!_.isEmpty)
      phone <- phoneGen
      fax <- phoneGen
      email <- emailGen
    } yield Contact(name, phone, fax, email)
  }

  implicit def phoneGen: Gen[String] = {
    for {
      p1 <- stringOfN(3, Gen.numChar)
      p2 <- stringOfN(3, Gen.numChar)
      p3 <- stringOfN(4, Gen.numChar)
      sep = "-"
    } yield List(p1, p2, p3).mkString(sep)
  }

  implicit def emailGen: Gen[String] = {
    for {
      name <- Gen.alphaStr.filter(s => s.nonEmpty)
      at = "@"
      domain <- Gen.alphaStr.filter(s => s.nonEmpty)
      dotCom = ".com"
    } yield List(name, at, domain, dotCom).mkString
  }
}
