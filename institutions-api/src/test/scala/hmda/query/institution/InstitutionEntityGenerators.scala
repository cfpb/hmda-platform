package hmda.query.institution

import org.scalacheck.Gen
import hmda.generators.CommonGenerators._

object InstitutionEntityGenerators {

  implicit def institutionEntityGen: Gen[InstitutionEntity] = {
    for {
      lei <- Gen.option(leiGen)
      activityYear <- activityYearGen
      agency <- agencyGen
      institutionType <- institutionTypeGen
      id2017 <- Gen.alphaStr
      taxId <- Gen.alphaStr
      rssd <- Gen.alphaStr
      emailDomains <- emailListGen.map(xs => xs.mkString(","))
      respondentName <- Gen.alphaStr.suchThat(!_.isEmpty)
      respondentState <- stateGen
      respondentCity <- Gen.alphaStr.suchThat(!_.isEmpty)
      parentIdRssd <- Gen.choose(Int.MinValue, Int.MaxValue)
      parentName <- Gen.alphaStr.suchThat(!_.isEmpty)
      topHolderIdRssd <- Gen.choose(Int.MinValue, Int.MaxValue)
      topHolderName <- Gen.alphaStr.suchThat(!_.isEmpty)
    } yield {
      InstitutionEntity(
        lei,
        activityYear,
        agency,
        institutionType,
        id2017,
        taxId,
        rssd,
        emailDomains,
        respondentName,
        respondentState,
        respondentCity,
        parentIdRssd,
        parentName,
        topHolderIdRssd,
        topHolderName
      )
    }
  }

  implicit def agencyGen: Gen[Int] = Gen.oneOf(List(1, 2, 3, 5, 7, 9, -1))

  implicit def institutionTypeGen: Gen[Int] =
    Gen.oneOf(
      List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, -1))

}
