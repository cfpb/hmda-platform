package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.enums.{
  ConstructionMethodEnum,
  LoanPurposeEnum,
  LoanTypeEnum,
  OccupancyEnum
}

case class Loan(
    ULI: Option[String] = None,
    applicationDate: String,
    loanType: LoanTypeEnum,
    loanPurpose: LoanPurposeEnum,
    constructionMethod: ConstructionMethodEnum,
    occupancy: OccupancyEnum,
    amount: Double,
    loanTerm: String
) extends PipeDelimited {
  override def toCSV: String = {
    val uliStr = ULI match {
      case Some(uli) => uli
      case None      => ""
    }
    s"$uliStr|$applicationDate|${loanType.code}|${loanPurpose.code}|${constructionMethod.code}|${occupancy.code}|$amount|$loanTerm"
  }
}
