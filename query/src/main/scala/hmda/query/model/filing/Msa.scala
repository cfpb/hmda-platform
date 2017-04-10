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
)

case class MsaWithName(
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
  def fromMsa(name: Option[String], msa: Msa): MsaWithName = {
    new MsaWithName(
      msa.id,
      name.getOrElse(""),
      msa.totalLars,
      msa.totalAmount,
      msa.conv,
      msa.FHA,
      msa.VA,
      msa.FSA,
      msa.oneToFourFamily,
      msa.MFD,
      msa.multiFamily,
      msa.homePurchase,
      msa.homeImprovement,
      msa.refinance
    )
  }
}

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
}

case class Irs(msas: List[MsaWithName], totals: MsaSummary)

case object Irs {
  def createIrs(msas: List[Msa]): Irs = {
    val codeMap = CbsaLookup.codeMap
    val msaSeqWithName = msas.map(m => MsaWithName().fromMsa(codeMap.get(m.id), m))

    val summary = msas.foldLeft(MsaSummary.empty) { (summary, msa) => summary + msa }

    Irs(msaSeqWithName, summary)
  }
}
