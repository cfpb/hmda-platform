package hmda.query.model.institutions

import org.scalacheck.Gen

object InstitutionQueryGenerators {

  implicit def institutionQueryGen: Gen[InstitutionQuery] = {
    for {
      id <- Gen.alphaStr
      agency <- Gen.alphaNumChar
      filingPeriod <- Gen.choose(2017, 2020)
      activityYear <- Gen.choose(2017, 2020)
      respondentId <- Gen.alphaStr
      institutionType <- Gen.alphaStr
      cra <- Gen.oneOf(true, false)
      emailDomain2015 <- Gen.alphaStr
      emailDomain2014 <- Gen.alphaStr
      emailDomain2013 <- Gen.alphaStr
      respondentName <- Gen.alphaStr
      respondentState <- Gen.alphaStr
      respondentCity <- Gen.alphaStr
      respondentFipsStateNumber <- Gen.alphaStr
      hmdaFilerFlag <- Gen.oneOf(true, false)
      parentRespondentId <- Gen.alphaStr
      parentIdRssd <- Gen.choose(0, 100)
      parentName <- Gen.alphaStr
      parentCity <- Gen.alphaStr
      parentState <- Gen.alphaStr
      assets <- Gen.choose(0, 100)
      otherLenderCode <- Gen.choose(0, 100)
      topHolderIdRssd <- Gen.choose(0, 100)
      topHolderName <- Gen.alphaStr
      topHolderCity <- Gen.alphaStr
      topHolderState <- Gen.alphaStr
      topHolderCountry <- Gen.alphaStr
    } yield InstitutionQuery(
      id,
      agency,
      filingPeriod,
      activityYear,
      respondentId,
      institutionType,
      cra,
      emailDomain2015,
      emailDomain2014,
      emailDomain2013,
      respondentName,
      respondentState,
      respondentCity,
      respondentFipsStateNumber,
      hmdaFilerFlag,
      parentRespondentId,
      parentIdRssd,
      parentName,
      parentCity,
      parentState,
      assets,
      otherLenderCode,
      topHolderIdRssd,
      topHolderName,
      topHolderCity,
      topHolderState,
      topHolderCountry
    )
  }
}
