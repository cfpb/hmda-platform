package hmda.model.institution

import org.scalacheck.Gen
import InstitutionGenerators._
import hmda.model.filing.FilingGenerator._

object InstitutionDetailGenerator {

  def institutionDetailGen: Gen[InstitutionDetail] = {
    for {
      institution <- institutionGen
      filings <- Gen.listOf(filingGen)
    } yield InstitutionDetail(Some(institution), filings)
  }
}
