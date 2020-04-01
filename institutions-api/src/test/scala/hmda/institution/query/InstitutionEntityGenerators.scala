package hmda.institution.query

import hmda.generators.CommonGenerators._
import org.scalacheck.Gen

object InstitutionEntityGenerators {

  implicit def institutionEntityGen: Gen[InstitutionEntity] = {
    for {
      lei <- leiGen
      activityYear <- activityYearGen
      agency <- agencyGen
      institutionType <- institutionTypeGen
      id2017 <- Gen.alphaStr
      taxId <- Gen.alphaStr
      rssd <- Gen.choose(0, Int.MaxValue)
      respondentName <- Gen.alphaStr.suchThat(!_.isEmpty)
      respondentState <- stateGen
      respondentCity <- Gen.alphaStr.suchThat(!_.isEmpty)
      parentIdRssd <- Gen.choose(Int.MinValue, Int.MaxValue)
      parentName <- Gen.alphaStr.suchThat(!_.isEmpty)
      assets <- Gen.choose(Int.MinValue, Int.MaxValue)
      otherLenderCode <- Gen.choose(Int.MinValue, Int.MaxValue)
      topHolderIdRssd <- Gen.choose(Int.MinValue, Int.MaxValue)
      topHolderName <- Gen.alphaStr.suchThat(!_.isEmpty)
      hmdaFiler <- Gen.oneOf(true, false)
    } yield {
      InstitutionEntity(
        lei,
        activityYear,
        agency,
        institutionType,
        id2017,
        taxId,
        rssd,
        respondentName,
        respondentState,
        respondentCity,
        parentIdRssd,
        parentName,
        assets,
        otherLenderCode,
        topHolderIdRssd,
        topHolderName,
        hmdaFiler
      )
    }
  }

  implicit def agencyGen: Gen[Int] = Gen.oneOf(List(1, 2, 3, 5, 7, 9))

  implicit def institutionTypeGen: Gen[Int] =
    Gen.oneOf(
      List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, -1))

}
