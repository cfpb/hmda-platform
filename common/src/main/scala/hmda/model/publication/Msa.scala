package hmda.model.publication
// $COVERAGE-OFF$
import hmda.model.filing.lar._
import hmda.model.filing.lar.enums._

import scala.language.implicitConversions
import scala.math.BigDecimal.RoundingMode

case class Msa(
    id: String = "",
    name: String = "",
    totalLars: Int = 0,
    totalAmount: BigDecimal = 0,
    conv: Int = 0,
    FHA: Int = 0,
    VA: Int = 0,
    FSA: Int = 0,
    siteBuilt: Int = 0,
    manufactured: Int = 0,
    oneToFour: Int = 0,
    fivePlus: Int = 0,
    homePurchase: Int = 0,
    homeImprovement: Int = 0,
    refinancing: Int = 0,
    cashOutRefinancing: Int = 0,
    otherPurpose: Int = 0,
    notApplicablePurpose: Int = 0
) {
  def addLar(lar: LoanApplicationRegister): Msa = {
    implicit def bool2int(b: Boolean): Int = if (b) 1 else 0

    val loanAmountBinned = 10000 * Math.floor(lar.loan.amount.toDouble / 10000) + 5000

    val loanAmountThousands = BigDecimal.valueOf(loanAmountBinned) / 1000

    Msa(
      id = id,
      name = name,
      totalLars = totalLars + 1,
      totalAmount = totalAmount + loanAmountThousands,
      conv = conv + (lar.loan.loanType == Conventional),
      FHA = FHA + (lar.loan.loanType == FHAInsured),
      VA = VA + (lar.loan.loanType == VAGuaranteed),
      FSA = FSA + (lar.loan.loanType == RHSOrFSAGuaranteed),
      siteBuilt = siteBuilt + (lar.loan.constructionMethod == SiteBuilt),
      manufactured = manufactured + (lar.loan.constructionMethod == ManufacturedHome),
      oneToFour = oneToFour + (lar.property.totalUnits <= 4),
      fivePlus = fivePlus + (lar.property.totalUnits >= 5),
      homePurchase = homePurchase + (lar.loan.loanPurpose == HomePurchase),
      homeImprovement = homeImprovement + (lar.loan.loanPurpose == HomeImprovement),
      refinancing = refinancing + (lar.loan.loanPurpose == Refinancing),
      cashOutRefinancing = cashOutRefinancing + (lar.loan.loanPurpose == CashOutRefinancing),
      otherPurpose = otherPurpose + (lar.loan.loanPurpose == OtherPurpose),
      notApplicablePurpose = notApplicablePurpose + (lar.loan.loanPurpose == LoanPurposeNotApplicable)
    )
  }

  def toCsv: String = {
    val amountRounded = totalAmount.setScale(0, RoundingMode.HALF_UP)
    s"""$id,\"$name\", $totalLars, $amountRounded, $conv, $FHA, $VA, $FSA, $siteBuilt, $manufactured, $oneToFour, $fivePlus, $homePurchase, $homeImprovement, $refinancing, $cashOutRefinancing, $otherPurpose, $notApplicablePurpose"""
  }

  def toPsvIDName: String = {
    val amountRounded = totalAmount.setScale(0, RoundingMode.HALF_UP)
    s"""$id,\"$name\", $totalLars, $amountRounded, $conv, $FHA, $VA, $FSA, $siteBuilt, $manufactured, $oneToFour, $fivePlus, $homePurchase, $homeImprovement, $refinancing, $cashOutRefinancing, $otherPurpose, $notApplicablePurpose"""
  }
}

case class MsaMap(msas: Map[String, Msa] = Map[String, Msa]()) {
  def addLar(lar: LoanApplicationRegister, msa: Msa): MsaMap = {
    val id = msa.id
    val original = msas.getOrElse(id, Msa(msa.id, msa.name))
    val modified = original.addLar(lar)
    MsaMap(msas + ((id, modified)))
  }
}

case class MsaSummary(
    lars: Int = 0,
    amount: BigDecimal = 0,
    conv: Int = 0,
    FHA: Int = 0,
    VA: Int = 0,
    FSA: Int = 0,
    siteBuilt: Int = 0,
    manufactured: Int = 0,
    oneToFour: Int = 0,
    fivePlus: Int = 0,
    homePurchase: Int = 0,
    homeImprovement: Int = 0,
    refinancing: Int = 0,
    cashOutRefinancing: Int = 0,
    otherPurpose: Int = 0,
    notApplicablePurpose: Int = 0
) {
  def +(elem: Msa): MsaSummary = {
    new MsaSummary(
      lars + elem.totalLars,
      amount + elem.totalAmount,
      conv + elem.conv,
      FHA + elem.FHA,
      VA + elem.VA,
      FSA + elem.FSA,
      siteBuilt + elem.siteBuilt,
      manufactured + elem.manufactured,
      oneToFour + elem.oneToFour,
      fivePlus + elem.fivePlus,
      homePurchase + elem.homePurchase,
      homeImprovement + elem.homeImprovement,
      refinancing + elem.refinancing,
      cashOutRefinancing + elem.cashOutRefinancing,
      otherPurpose + elem.otherPurpose,
      notApplicablePurpose + elem.notApplicablePurpose
    )
  }

  def toCsv: String = {
    val amountRounded = amount.setScale(0, RoundingMode.HALF_UP)
    s"Totals,, $lars, $amountRounded, $conv, $FHA, $VA, $FSA, $siteBuilt, $manufactured, $oneToFour, $fivePlus, $homePurchase, $homeImprovement, $refinancing, $cashOutRefinancing, $otherPurpose, $notApplicablePurpose"
  }
}

case object MsaSummary {
  def fromMsaCollection(msas: Seq[Msa]): MsaSummary = {
    msas.foldLeft(MsaSummary()) { (summary, msa) =>
      summary + msa
    }
  }
}
// $COVERAGE-OFF$