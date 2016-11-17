package hmda.model.institution

import org.scalacheck.Gen

object InstitutionGenerators {
  implicit def institutionStatusGen: Gen[InstitutionStatus] = {
    Gen.oneOf(Active, Inactive)
  }

  implicit def institutionGen: Gen[Institution] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      externalIds <- Gen.listOf(externalIdGen)
      status <- institutionStatusGen
      agency <- agencyGen
      active <- Gen.oneOf(true, false)
      cra <- Gen.oneOf(true, false)
      institutionType <- institutionTypeGen
    } yield Institution(id, name, externalIds.toSet, agency, institutionType, active, cra, status)
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
