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
      emailDomains <- Gen.listOf(Gen.alphaStr)
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
    } yield Institution(
      id,
      agency,
      activityYear,
      institutionType,
      cra = cra,
      externalIds.toSet,
      emailDomains.toSet,
      Respondent(respondentId, respondentName, respondentState, respondentCity, respondentFipsStateNumber),
      hmdaFilerFlag = hmdaFilerFlag,
      Parent(parentRespondentId, parentIdRssd, parentName, parentCity, parentState),
      assets,
      otherLenderCode,
      TopHolder(topHolderIdRssd, topHolderName, topHolderCity, topHolderState, topHolderCountry)
    )
  }

  def sampleInstitution: Institution = institutionGen.sample.getOrElse(Institution.empty)

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
