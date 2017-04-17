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
}

case class Irs(msas: List[MsaWithName], totals: MsaSummary)

case object Irs {
  def createIrs(msas: List[Msa]): Irs = {
    val codeMap = CbsaLookup.codeMap
    val msaSeqWithName = msas.map(m => addMsaName(m, codeMap.get(m.id)))

    val summary = msas.foldLeft(MsaSummary.empty) { (summary, msa) => summary + msa }

    Irs(msaSeqWithName, summary)
  }

  private def addMsaName(msa: Msa, name: Option[String]): MsaWithName = {
    MsaWithName(
      msa.id,
      name.getOrElse("NA"),
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
