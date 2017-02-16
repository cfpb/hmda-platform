package hmda.query.model.filing

case class Msa(
  id: Int,
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
)

case class Irs(msas: List[Msa], msaSummary: MsaSummary)
