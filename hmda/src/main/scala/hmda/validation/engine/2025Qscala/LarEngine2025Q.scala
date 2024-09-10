package hmda.validation.engine

// $COVERAGE-OFF$
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.census.records.CensusRecords
import hmda.validation.rules.lar.validity._
import hmda.validation.engine.LarEngine2025

private[engine] object LarEngine2025Q extends ValidationEngine[LoanApplicationRegister] {

  override def syntacticalChecks(ctx: ValidationContext) = LarEngine2025.syntacticalChecks(ctx)

  override def validityChecks(ctx: ValidationContext) = LarEngine2025.validityChecks(ctx) ++ Vector(V627.withIndexedCounties(CensusRecords.indexedCounty2024))

  override def qualityChecks(ctx: ValidationContext) = LarEngine2025.qualityChecks(ctx: ValidationContext)

}
// $COVERAGE-ON$
