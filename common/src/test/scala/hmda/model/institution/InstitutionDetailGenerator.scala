package hmda.model.institution

import hmda.model.filing.FilingGenerator._
import hmda.model.institution.InstitutionGenerators._
import org.scalacheck.Gen

object InstitutionDetailGenerator {

  def institutionDetailGen: Gen[InstitutionDetail] = {
    for {
      institution <- institutionGen
      filings <- Gen.listOf(filingGen)
    } yield InstitutionDetail(Some(institution), filings)
  }
}
