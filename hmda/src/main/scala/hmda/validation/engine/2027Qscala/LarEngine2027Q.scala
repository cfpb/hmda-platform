package hmda.validation.engine

// $COVERAGE-OFF$
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.census.records.CensusRecords
import hmda.validation.rules.lar.validity._

private[engine] object LarEngine2027Q extends ValidationEngine[LoanApplicationRegister] {

  override def syntacticalChecks(ctx: ValidationContext) = LarEngine2027.syntacticalChecks(ctx)

  override def validityChecks(ctx: ValidationContext) = LarEngine2027.validityChecks(ctx) ++ Vector(V627.withIndexedCounties(CensusRecords.indexedCounty2027))

  override def qualityChecks(ctx: ValidationContext) = LarEngine2027.qualityChecks(ctx: ValidationContext)

}
// $COVERAGE-ON$
