package hmda.model.filing.lar

case class LoanDisclosure(totalLoanCosts: String,
                          totalPointsAndFees: String,
                          originationCharges: Option[String] = None,
                          discountPoints: Option[String] = None,
                          lenderCredits: Option[String] = None)
