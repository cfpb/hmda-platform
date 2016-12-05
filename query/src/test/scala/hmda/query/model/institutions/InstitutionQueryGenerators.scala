package hmda.query.model.institutions

import org.scalacheck.Gen

object InstitutionQueryGenerators {

  implicit def institutionQueryGen: Gen[InstitutionEntity] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      cra <- Gen.oneOf(true, false)
      agency <- Gen.alphaNumChar
      institutionType <- Gen.alphaStr
      parent <- Gen.oneOf(true, false)
      status <- Gen.oneOf(1, 2)
      filingPeriod <- Gen.choose(2017, 2020)
    } yield InstitutionEntity(
      id,
      name,
      cra,
      agency,
      institutionType,
      parent,
      status,
      filingPeriod
    )
  }
}
