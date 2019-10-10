package hmda.model.filing.lar

import io.circe._

case class LoanDisclosure(totalLoanCosts: String = "",
                          totalPointsAndFees: String = "",
                          originationCharges: String = "",
                          discountPoints: String = "",
                          lenderCredits: String = "")

object LoanDisclosure {
  implicit val loanDisclosureEncoder: Encoder[LoanDisclosure] =
    (a: LoanDisclosure) =>
      Json.obj(
        ("totalLoanCosts", Json.fromString(a.totalLoanCosts)),
        ("totalPointsAndFees", Json.fromString(a.totalPointsAndFees)),
        ("originationCharges", Json.fromString(a.originationCharges)),
        ("discountPoints", Json.fromString(a.discountPoints)),
        ("lenderCredits", Json.fromString(a.lenderCredits))
      )

  implicit val loanDisclosureDecoder: Decoder[LoanDisclosure] =
    (c: HCursor) =>
      for {
        totalLoanCosts <- c.downField("totalLoanCosts").as[String]
        totalPointsAndFees <- c.downField("totalPointsAndFees").as[String]
        originationCharges <- c.downField("originationCharges").as[String]
        discountPoints <- c.downField("discountPoints").as[String]
        lenderCredits <- c.downField("lenderCredits").as[String]
      } yield
        LoanDisclosure(totalLoanCosts,
          totalPointsAndFees,
          originationCharges,
          discountPoints,
          lenderCredits)
}