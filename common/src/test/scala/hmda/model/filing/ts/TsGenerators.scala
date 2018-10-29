package hmda.model.filing.ts

import hmda.generators.CommonGenerators._
import hmda.model.institution.InstitutionGenerators._
import org.scalacheck.Gen

object TsGenerators {

  implicit def tsGen: Gen[TransmittalSheet] = {
    for {
      institutionName <- Gen.alphaStr
      year <- activityYearGen
      contact <- contactGen
      agency <- agencyGen
      totalLines <- totalLinesGen
      taxId <- taxIdGen
      lei <- leiGen
    } yield
      TransmittalSheet(
        institutionName = institutionName,
        year = year,
        contact = contact,
        agency = agency,
        totalLines = totalLines,
        taxId = taxId,
        LEI = lei
      )
  }

  implicit def contactGen: Gen[Contact] = {
    for {
      name <- Gen.alphaStr
      phone <- phoneGen
      email <- emailGen
      address <- addressGen
    } yield Contact(name, phone, email, address)
  }

  implicit def addressGen: Gen[Address] = {
    for {
      street <- Gen.alphaStr
      city <- Gen.alphaStr
      state <- stateGen
      zipCode <- zipGen
    } yield Address(street, city, state, zipCode)
  }

  implicit def totalLinesGen: Gen[Int] = {
    Gen.choose(1, Int.MaxValue)
  }

  implicit def taxIdGen: Gen[String] = {
    for {
      prefix <- stringOfN(2, Gen.numChar)
      sep = "-"
      suffix <- stringOfN(7, Gen.numChar)
    } yield List(prefix, suffix).mkString(sep)
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

  implicit def phoneGen: Gen[String] = {
    for {
      p1 <- stringOfN(3, Gen.numChar)
      p2 <- stringOfN(3, Gen.numChar)
      p3 <- stringOfN(4, Gen.numChar)
      sep = "-"
    } yield List(p1, p2, p3).mkString(sep)
  }

}
