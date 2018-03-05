package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited

case class LoanDisclosure(totalLoanCosts: String,
                          totalPointsAndFees: String,
                          originationCharges: Option[String] = None,
                          discountPoints: Option[String] = None,
                          lenderCredits: Option[String] = None)
    extends PipeDelimited {
  override def toCSV: String = {

    val originationChargesStr = originationCharges match {
      case Some(charges) => charges
      case None          => ""
    }

    val discountPointsStr = discountPoints match {
      case Some(points) => points
      case None         => ""
    }

    val lenderCreditsStr = lenderCredits match {
      case Some(credits) => credits
      case None          => ""
    }

    s"$totalLoanCosts|$totalPointsAndFees|$originationChargesStr|$discountPointsStr|$lenderCreditsStr"
  }
}
