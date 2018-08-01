package hmda.model.filing

import hmda.model.institution.Agency
import org.scalacheck.Gen

object FilingGenerators {

  implicit def agencyCodeGen: Gen[Int] = {
    Gen.oneOf(Agency.values.filter(x => x != -1))
  }

  implicit def agencyGen: Gen[Agency] = {
    for {
      agencyCode <- agencyCodeGen
      agency = Agency.valueOf(agencyCode)
    } yield agency
  }

}
