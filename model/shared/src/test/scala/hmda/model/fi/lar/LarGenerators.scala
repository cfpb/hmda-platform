package hmda.model.fi.lar

import hmda.model.fi.FIGenerators
import org.scalacheck.Gen

trait LarGenerators extends FIGenerators {

  def larNGen(n: Int): Gen[List[LoanApplicationRegister]] = {
    Gen.listOfN(n, larGen)
  }

  implicit def lar100ListGen: Gen[List[LoanApplicationRegister]] = {
    Gen.listOfN(100, larGen)
  }

  implicit def larListGen: Gen[List[LoanApplicationRegister]] = {
    Gen.listOfN(10, larGen)
  }

  implicit def larGen: Gen[LoanApplicationRegister] = {
    for {
      respondent <- respIdGen
      agencyCode <- agencyCodeGen
      loan <- loanGen
      preapprovals <- preapprovalGen
      actionTaken <- actionTypeGen
      actionTakenDate <- actionDateGen
      geography <- geographyGen
      applicant <- applicantGen
      purchaserType <- purchaserTypeGen
      denial <- denialGen
      rateSpread <- rateSpreadGen
      hoepaStatus <- hoepaStatusGen
      lienStatus <- lienStatusGen
    } yield LoanApplicationRegister(
      2, // TODO we may not want to hard-code this everywhere (though it may be appropriate here)
      respondent,
      agencyCode,
      loan,
      preapprovals,
      actionTaken,
      actionTakenDate,
      geography,
      applicant,
      purchaserType,
      denial,
      rateSpread,
      hoepaStatus,
      lienStatus
    )
  }

  def sampleLar: LoanApplicationRegister = larGen.sample.getOrElse(LoanApplicationRegister())

  implicit def loanGen: Gen[Loan] = {
    for {
      id <- stringOfOneToN(25, Gen.alphaChar)
      applicationDate <- optional(dateGen, "NA")
      loanType <- Gen.oneOf(1, 2, 3, 4)
      propertyType <- Gen.oneOf(1, 2, 3)
      purpose <- Gen.oneOf(1, 2, 3)
      occupancy <- Gen.oneOf(1, 2, 3)
      amount <- Gen.choose(1, 100000)
    } yield Loan(
      id,
      applicationDate,
      loanType,
      propertyType,
      purpose,
      occupancy,
      amount
    )
  }

  implicit def preapprovalGen: Gen[Int] = Gen.oneOf(1, 2, 3)

  implicit def actionTypeGen: Gen[Int] = Gen.choose(1, 8)

  implicit def actionDateGen: Gen[Int] = dateGen

  implicit def geographyGen: Gen[Geography] = {
    for {
      msa <- optional(stringOfN(5, Gen.numChar), "NA")
      state <- optional(stateCodeGen, "NA")
      county <- optional(stringOfN(3, Gen.numChar), "NA")
      tract <- optional(censusTractGen, "NA")
    } yield Geography(
      msa,
      state,
      county,
      tract
    )
  }

  // see http://www2.census.gov/geo/docs/reference/state.txt
  // Only includes states + DC and PR (HMDA Filers)
  implicit def stateCodeGen: Gen[String] = {
    Gen.oneOf(
      "01", "02", "04", "05", "06", "08", "09",
      "10", "11", "12", "13", "15", "16", "17", "18", "19",
      "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
      "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
      "40", "41", "42", "44", "45", "46", "47", "48", "49",
      "50", "51", "53", "54", "55", "56",
      "72"
    )
  }

  implicit def censusTractGen = {
    for {
      tract <- stringOfN(4, Gen.numChar) // with or without leading zeroes
      suffix <- stringOfN(2, Gen.numChar)
    } yield List(tract, suffix).mkString(".")
  }

  implicit def applicantGen: Gen[Applicant] = {
    for {
      ethnicity <- Gen.choose(1, 4)
      coEthnicity <- Gen.choose(1, 5)
      race1 <- Gen.choose(1, 7)
      (race2 :: race3 :: race4 :: race5 :: _) <- Gen.listOfN(4, optional(Gen.choose(1, 5)))
      coRace1 <- Gen.choose(1, 8)
      (coRace2 :: coRace3 :: coRace4 :: coRace5 :: _) <- Gen.listOfN(4, optional(Gen.choose(1, 5)))
      sex <- Gen.choose(1, 4)
      coSex <- Gen.choose(1, 5)
      income <- optional(Gen.choose(1, 9999), "NA")
    } yield Applicant(
      ethnicity,
      coEthnicity,
      race1, race2, race3, race4, race5,
      coRace1, coRace2, coRace3, coRace4, coRace5,
      sex,
      coSex,
      income
    )
  }

  implicit def purchaserTypeGen: Gen[Int] = Gen.choose(0, 9)

  implicit def denialGen: Gen[Denial] = {
    for {
      (reason1 :: reason2 :: reason3 :: _) <- Gen.listOfN(3, optional(Gen.choose(1, 9)))
    } yield Denial(
      reason1,
      reason2,
      reason3
    )
  }

  implicit def rateSpreadGen: Gen[String] = {
    val numericSpreadGen = Gen.listOfN(2, stringOfN(2, Gen.numChar)).map(_.mkString("."))
    optional(numericSpreadGen, "NA")
  }

  implicit def hoepaStatusGen: Gen[Int] = Gen.oneOf(1, 2)

  implicit def lienStatusGen: Gen[Int] = Gen.oneOf(1, 2, 3, 4)
}
