package hmda.parser.fi.lar

import hmda.model.fi.lar._
import hmda.parser.fi.FIGenerators
import org.scalacheck.Gen

trait LarGenerators extends FIGenerators {

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

  implicit def loanGen: Gen[Loan] = {
    for {
      id <- Gen.listOf(Gen.alphaNumChar).map(_.mkString)
      //applicationDate <- ??? // TODO Format is ccyymmdd or NA
      loanType <- Gen.oneOf(1, 2, 3, 4)
      propertyType <- Gen.oneOf(1, 2, 3)
      purpose <- Gen.oneOf(1, 2, 3)
      occupancy <- Gen.oneOf(1, 2, 3)
      amount <- Gen.posNum[Int] // TODO is that right? can it be zero? (what would a loan for zero thousand dollars mean?)
    } yield Loan(
      id,
      "NA",
      loanType,
      propertyType,
      purpose,
      occupancy,
      amount
    )
  }

  implicit def preapprovalGen: Gen[Int] = Gen.oneOf(1, 2, 3)

  implicit def actionTypeGen: Gen[Int] = Gen.choose(1, 8)

  implicit def actionDateGen: Gen[Int] = Gen.choose(20170101, 20201231) // TODO this allows non-date numbers!
  // so play with things like LocalDate.of(2017, 01, 01) and figure out how to convert

  implicit def geographyGen: Gen[Geography] = { // TODO any of these can be NA
    for {
      msa <- stringOfN(5, Gen.numChar) // actually it's more specific; do we care, for this purpose?
      state <- stringOfN(2, Gen.numChar) // TODO limit to real ones? http://www2.census.gov/geo/docs/reference/state.txt
      county <- stringOfN(3, Gen.numChar)
      tract <- censusTractGen
    } yield Geography(
      msa,
      state,
      county,
      tract
    )
  }

  implicit def censusTractGen = {
    for {
      tract <- stringOfN(4, Gen.numChar).map(Some(_)) // TODO are leading zeroes required? (they are allowed.)
      suffix <- Gen.option(stringOfN(2, Gen.numChar))
    } yield List(tract, Some("."), suffix).flatten.mkString
  }

  implicit def applicantGen: Gen[Applicant] = {
    for {
      ethnicity <- Gen.choose(1, 4)
      coEthnicity <- Gen.choose(1, 5)
      race1 <- Gen.choose(1, 7)
      //coRace <-  TODO this should be 8 if and only if coEthnicity is 5 (i.e. no co-applicant). do we care?
    } yield Applicant(
      ethnicity, coEthnicity, race1, "", "", "", "", 2, "", "", "", "", 2, 2, "NA" // TODO implement for real
    )
  }

  implicit def purchaserTypeGen: Gen[Int] = Gen.choose(0, 9)

  implicit def denialGen: Gen[Denial] = { // yes, I do plan to clean up this code.
    for {
      reason1 <- Gen.option(Gen.choose(1, 9)).map(_.map(_.toString)) // TODO are these values independent or related?
      reason2 <- Gen.option(Gen.choose(1, 9)).map(_.map(_.toString))
      reason3 <- Gen.option(Gen.choose(1, 9)).map(_.map(_.toString))
    } yield Denial(
      reason1.getOrElse(""),
      reason2.getOrElse(""),
      reason3.getOrElse("")
    )
  }

  implicit def rateSpreadGen: Gen[String] = { // TODO or it can be NA
    for {
      before <- stringOfN(2, Gen.numChar)
      after <- stringOfN(2, Gen.numChar)
    } yield List(before, after).mkString(".")
  }

  implicit def hoepaStatusGen: Gen[Int] = Gen.oneOf(1, 2)

  implicit def lienStatusGen: Gen[Int] = Gen.oneOf(1, 2, 3, 4)

  // TODO this is quite general and probably belongs elsewhere (if it turns out to be useful in clean/idiomatic code)
  def stringOfN(n: Int, genOne: Gen[Char]): Gen[String] = {
    Gen.listOfN(n, genOne).map(_.mkString)
  }
}
