package hmda.validation.engine

// $COVERAGE-OFF$
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.census.records.CensusRecords
import hmda.validation.rules.lar.validity._
import hmda.validation.engine.LarEngine2025

private[engine] object LarEngine2026Q extends ValidationEngine[LoanApplicationRegister] {

  override def syntacticalChecks(ctx: ValidationContext) = LarEngine2026.syntacticalChecks(ctx)

  override def validityChecks(ctx: ValidationContext) = LarEngine2026.validityChecks(ctx) ++ Vector(V627.withIndexedCounties(CensusRecords.indexedCounty2026))

  override def qualityChecks(ctx: ValidationContext) = LarEngine2026.qualityChecks(ctx: ValidationContext)

}
// $COVERAGE-ON$
