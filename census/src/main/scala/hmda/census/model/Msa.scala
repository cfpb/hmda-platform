package hmda.census.model

import hmda.model.fi.lar.LoanApplicationRegister

import scala.language.implicitConversions

case class Msa(
    id: String = "",
    name: String = "",
    totalLars: Int = 0,
    totalAmount: Int = 0,
    conv: Int = 0,
    FHA: Int = 0,
    VA: Int = 0,
    FSA: Int = 0,
    oneToFourFamily: Int = 0,
    MFD: Int = 0,
    multiFamily: Int = 0,
    homePurchase: Int = 0,
    homeImprovement: Int = 0,
    refinance: Int = 0
) {
  def addLar(lar: LoanApplicationRegister): Msa = {
    implicit def bool2int(b: Boolean): Int = if (b) 1 else 0

    new Msa(
      id = id,
      name = name,
      totalLars = totalLars + 1,
      totalAmount = totalAmount + lar.loan.amount,
      conv = conv + (lar.loan.loanType == 1),
      FHA = FHA + (lar.loan.loanType == 2),
      VA = VA + (lar.loan.loanType == 3),
      FSA = FSA + (lar.loan.loanType == 4),
      oneToFourFamily = oneToFourFamily + (lar.loan.propertyType == 1),
      MFD = MFD + (lar.loan.propertyType == 2),
      multiFamily = multiFamily + (lar.loan.propertyType == 3),
      homePurchase = homePurchase + (lar.loan.purpose == 1),
      homeImprovement = homeImprovement + (lar.loan.purpose == 2),
      refinance = refinance + (lar.loan.purpose == 3)
    )
  }

  def toCsv: String = s"$id, $name, $totalLars, $totalAmount, $conv, $FHA, $VA, $FSA, $oneToFourFamily, $MFD, $multiFamily, $homePurchase, $homeImprovement, $refinance"
}

case class MsaMap(
    msas: Map[String, Msa] = Map[String, Msa]()
) {
  def +(lar: LoanApplicationRegister): MsaMap = {
    val id = lar.geography.msa
    val original = msas.getOrElse(id, Msa(id, CbsaLookup.nameFor(id)))
    val modified = original.addLar(lar)
    MsaMap(msas + ((id, modified)))
  }
}

case class MsaSummary(
    lars: Int = 0,
    amount: Int = 0,
    conv: Int = 0,
    FHA: Int = 0,
    VA: Int = 0,
    FSA: Int = 0,
    oneToFourFamily: Int = 0,
    MFD: Int = 0,
    multiFamily: Int = 0,
    homePurchase: Int = 0,
    homeImprovement: Int = 0,
    refinance: Int = 0
) {
  def +(elem: Msa): MsaSummary = {
    new MsaSummary(
      lars + elem.totalLars,
      amount + elem.totalAmount,
      conv + elem.conv,
      FHA + elem.FHA,
      VA + elem.VA,
      FSA + elem.FSA,
      oneToFourFamily + elem.oneToFourFamily,
      MFD + elem.MFD,
      multiFamily + elem.multiFamily,
      homePurchase + elem.homePurchase,
      homeImprovement + elem.homeImprovement,
      refinance + elem.refinance
    )
  }

  def toCsv: String = s"Totals,, $lars, $amount, $conv, $FHA, $VA, $FSA, $oneToFourFamily, $MFD, $multiFamily, $homePurchase, $homeImprovement, $refinance"
}

case object MsaSummary {
  def fromMsaCollection(msas: Seq[Msa]): MsaSummary = {
    msas.foldLeft(MsaSummary()) { (summary, msa) => summary + msa }
  }
}
