package hmda.model.institution

import org.scalacheck.Gen

object InstitutionGenerators {

  implicit def institutionGen: Gen[Institution] = {
    for {
      id <- Gen.alphaStr
      agency <- agencyGen
      activityYear <- Gen.choose(2000, 2020)
      respondentId <- externalIdGen
      institutionType <- institutionTypeGen
      cra <- Gen.oneOf(true, false)
      externalIds <- Gen.listOf(externalIdGen)
      emailDomain2015 <- Gen.alphaStr
      emailDomain2014 <- Gen.alphaStr
      emailDomain2013 <- Gen.alphaStr
      respondentName <- Gen.alphaStr
      respondentState <- Gen.alphaStr
      respondentCity <- Gen.alphaStr
      respondentFipsStateNumber <- Gen.alphaStr
      hmdaFilerFlag <- Gen.oneOf(true, false)
      parentRespondentId <- Gen.alphaStr
      parentIdRssd <- Gen.numStr
      parentName <- Gen.alphaStr
      parentCity <- Gen.alphaStr
      parentState <- Gen.alphaStr
      assets <- Gen.numStr
      otherLenderCode <- Gen.numStr
      topHolderIdRssd <- Gen.numStr
      topHolderName <- Gen.alphaStr
      topHolderCity <- Gen.alphaStr
      topHolderState <- Gen.alphaStr
      topHolderCountry <- Gen.alphaStr
    } yield Institution(id, agency, activityYear, respondentId, institutionType, cra = cra,
      externalIds.toSet, emailDomain2015, emailDomain2014, emailDomain2013, respondentName,
      respondentState, respondentCity, respondentFipsStateNumber, hmdaFilerFlag = hmdaFilerFlag,
      parentRespondentId, parentIdRssd.toInt, parentName, parentCity, parentState, assets.toInt,
      otherLenderCode.toInt, topHolderIdRssd.toInt, topHolderName, topHolderCity, topHolderState,
      topHolderCountry)
  }

  implicit def agencyGen: Gen[Agency] = {
    Gen.oneOf(
      Agency.values
    )
  }

  implicit def institutionTypeGen: Gen[InstitutionType] = {
    Gen.oneOf(
      InstitutionType.values
    )
  }

  implicit def externalIdGen: Gen[ExternalId] = {
    for {
      id <- Gen.alphaStr
      idType <- externalIdTypeGen
    } yield ExternalId(id, idType)
  }

  implicit def externalIdTypeGen: Gen[ExternalIdType] = {
    Gen.oneOf(
      ExternalIdType.values
    )
  }
}
