package hmda.persistence.model

import hmda.census.model.Msa
import org.scalacheck.Gen

trait MsaGenerators {
  def listOfMsaGen: Gen[List[Msa]] = {
    Gen.listOf(msaGen)
  }

  def msaGen: Gen[Msa] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      totalLars <- Gen.choose(0, 1000)
      totalAmount <- Gen.choose(0, 1000)
      conv <- Gen.choose(0, 1000)
      fha <- Gen.choose(0, 1000)
      va <- Gen.choose(0, 1000)
      fsa <- Gen.choose(0, 1000)
      oneToFourFamily <- Gen.choose(0, 1000)
      mfd <- Gen.choose(0, 1000)
      multiFamily <- Gen.choose(0, 1000)
      homePurchase <- Gen.choose(0, 1000)
      homeImprovement <- Gen.choose(0, 1000)
      refinance <- Gen.choose(0, 1000)
    } yield Msa(
      id,
      name,
      totalLars,
      totalAmount,
      conv,
      fha,
      va,
      fsa,
      oneToFourFamily,
      mfd,
      multiFamily,
      homePurchase,
      homeImprovement,
      refinance
    )
  }
}
