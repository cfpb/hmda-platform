package hmda.query.model.filing

import hmda.census.model.CbsaLookup

case class Msa(
    id: String,
    totalLars: Int,
    totalAmount: Int,
    conv: Int,
    FHA: Int,
    VA: Int,
    FSA: Int,
    oneToFourFamily: Int,
    MFD: Int,
    multiFamily: Int,
    homePurchase: Int,
    homeImprovement: Int,
    refinance: Int
) {
  def addName: MsaWithName = MsaWithName(
    this.id,
    CbsaLookup.nameFor(this.id),
    this.totalLars,
    this.totalAmount,
    this.conv,
    this.FHA,
    this.VA,
    this.FSA,
    this.oneToFourFamily,
    this.MFD,
    this.multiFamily,
    this.homePurchase,
    this.homeImprovement,
    this.refinance
  )
}

case class MsaWithName(
  id: String,
  name: String,
  totalLars: Int,
  totalAmount: Int,
  conv: Int,
  FHA: Int,
  VA: Int,
  FSA: Int,
  oneToFourFamily: Int,
  MFD: Int,
  multiFamily: Int,
  homePurchase: Int,
  homeImprovement: Int,
  refinance: Int
)

case class MsaSummary(
    lars: Int,
    amount: Int,
    conv: Int,
    FHA: Int,
    VA: Int,
    FSA: Int,
    oneToFourFamily: Int,
    MFD: Int,
    multiFamily: Int,
    homePurchase: Int,
    homeImprovement: Int,
    refinance: Int
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
}

case object MsaSummary {
  def empty: MsaSummary = MsaSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

  def fromMsaCollection(msas: Seq[Msa]): MsaSummary = {
    msas.foldLeft(MsaSummary.empty) { (summary, msa) => summary + msa }
  }
}
