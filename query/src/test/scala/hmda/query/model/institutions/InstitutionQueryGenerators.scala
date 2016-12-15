package hmda.query.model.institutions

import org.scalacheck.Gen

object InstitutionQueryGenerators {

  implicit def institutionQueryGen: Gen[InstitutionQuery] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      cra <- Gen.oneOf(true, false)
      agency <- Gen.alphaNumChar
      institutionType <- Gen.alphaStr
      parent <- Gen.oneOf(true, false)
      filingPeriod <- Gen.choose(2017, 2020)
    } yield InstitutionQuery(
      id,
      name,
      cra,
      agency,
      institutionType,
      parent,
      filingPeriod
    )
  }
}
