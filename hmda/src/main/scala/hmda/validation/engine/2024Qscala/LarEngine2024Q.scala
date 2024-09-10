package hmda.validation.engine

// $COVERAGE-OFF$
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.census.records.CensusRecords
import hmda.validation.rules.lar.validity._
import hmda.validation.engine.LarEngine2024

private[engine] object LarEngine2024Q extends ValidationEngine[LoanApplicationRegister] {

  override def syntacticalChecks(ctx: ValidationContext) = LarEngine2024.syntacticalChecks(ctx)

  override def validityChecks(ctx: ValidationContext) = LarEngine2024.validityChecks(ctx) ++ Vector(V627.withIndexedCounties(CensusRecords.indexedCounty2024))

  override def qualityChecks(ctx: ValidationContext) = LarEngine2024.qualityChecks(ctx: ValidationContext)
  
}
// $COVERAGE-ON$
