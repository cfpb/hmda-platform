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
      respondent <- respondentGen
      hmdaFilerFlag <- Gen.oneOf(true, false)
      parent <- parentGen
      assets <- Gen.choose(0, 100)
      otherLenderCode <- Gen.choose(0, 100)
      topHolder <- topHolderGen
    } yield Institution(
      id,
      agency,
      activityYear,
      institutionType,
      cra = cra,
      externalIds.toSet,
      emailDomains.toSet,
      respondent,
      hmdaFilerFlag = hmdaFilerFlag,
      parent,
      assets,
      otherLenderCode,
      topHolder
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

  implicit def respondentGen: Gen[Respondent] = {
    for {
      respondentId <- externalIdGen
      respondentName <- Gen.alphaStr
      respondentState <- Gen.alphaStr
      respondentCity <- Gen.alphaStr
      respondentFipsStateNumber <- Gen.alphaStr
    } yield Respondent(respondentId, respondentName, respondentState, respondentCity, respondentFipsStateNumber)
  }

  implicit def parentGen: Gen[Parent] = {
    for {
      parentRespondentId <- Gen.alphaStr
      parentIdRssd <- Gen.choose(0, 100)
      parentName <- Gen.alphaStr
      parentCity <- Gen.alphaStr
      parentState <- Gen.alphaStr
    } yield Parent(parentRespondentId, parentIdRssd, parentName, parentCity, parentState)
  }

  implicit def topHolderGen: Gen[TopHolder] = {
    for {
      topHolderIdRssd <- Gen.choose(0, 100)
      topHolderName <- Gen.alphaStr
      topHolderCity <- Gen.alphaStr
      topHolderState <- Gen.alphaStr
      topHolderCountry <- Gen.alphaStr
    } yield TopHolder(topHolderIdRssd, topHolderName, topHolderCity, topHolderState, topHolderCountry)
  }
}
