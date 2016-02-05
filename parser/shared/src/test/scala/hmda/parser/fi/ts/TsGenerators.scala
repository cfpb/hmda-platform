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
      prefix <- Gen.numStr
      sep = "-"
      suffix <- Gen.numStr
    } yield List(prefix.take(2), sep, suffix.take(7)).mkString
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
      zip <- Gen.numStr
    } yield zip.take(5)
  }

  implicit def zipPlus4Gen: Gen[String] = {
    for {
      zip <- Gen.numStr
      plus <- Gen.numStr
      sep = "-"
    } yield List(zip.take(5), sep, plus.take(4)).mkString
  }

  implicit def contactGen: Gen[Contact] = {
    for {
      name <- Gen.alphaStr
      phone <- phoneGen
      fax <- phoneGen
      email <- emailGen
    } yield Contact(name, phone, fax, email)
  }

  implicit def phoneGen: Gen[String] = {
    for {
      p1 <- Gen.numStr
      p2 <- Gen.numStr
      p3 <- Gen.numStr
      sep = "-"
    } yield List(p1.take(3).toString, sep, p2.take(3).toString, sep, p3.take(4).toString).mkString
  }

  implicit def emailGen: Gen[String] = {
    for {
      name <- Gen.alphaStr
      at = "@"
      domain <- Gen.alphaStr
      dotCom = ".com"
    } yield List(name, at, domain, dotCom).mkString
  }

}
