package hmda.parser.fi.ts

import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }
import hmda.parser.fi.FIGenerators
import hmda.parser.util.FITestData._
import org.scalacheck.Gen

trait TsGenerators extends FIGenerators {

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
      prefix <- Gen.listOfN(2, Gen.numChar)
      sep = "-"
      suffix <- Gen.listOfN(7, Gen.numChar)
    } yield List(prefix, suffix).map(_.mkString).mkString(sep)
  }

  implicit def respondentGen: Gen[Respondent] = {
    for {
      id <- respIdGen
      name <- Gen.alphaStr
      address <- Gen.alphaStr
      city <- Gen.alphaStr
      state <- stateGen
      zip <- zipGen
    } yield Respondent(id, name, address, city, state, zip)
  }

  implicit def parentGen: Gen[Parent] = {
    for {
      name <- Gen.alphaStr
      address <- Gen.alphaStr
      city <- Gen.alphaStr
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

  implicit def zip5Gen: Gen[String] = {
    for {
      zip <- Gen.listOfN(5, Gen.numChar)
    } yield zip.mkString
  }

  implicit def zipPlus4Gen: Gen[String] = {
    for {
      zip <- Gen.listOfN(5, Gen.numChar)
      plus <- Gen.listOfN(4, Gen.numChar)
      sep = "-"
    } yield List(zip, plus).map(_.mkString).mkString(sep)
  }

  implicit def contactGen: Gen[Contact] = {
    for {
      name <- Gen.alphaStr.filter(!_.isEmpty)
      phone <- phoneGen
      fax <- phoneGen
      email <- emailGen
    } yield Contact(name, phone, fax, email)
  }

  implicit def phoneGen: Gen[String] = {
    for {
      p1 <- Gen.listOfN(3, Gen.numChar)
      p2 <- Gen.listOfN(3, Gen.numChar)
      p3 <- Gen.listOfN(4, Gen.numChar)
      sep = "-"
    } yield List(p1, p2, p3).map(_.mkString).mkString(sep)
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
