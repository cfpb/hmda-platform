package hmda.model.filing.lar

import hmda.model.filing.lar.enums.AutomatedUnderwritingResultEnum

case class AutomatedUnderwritingSystemResult(
                                              ausResult1: AutomatedUnderwritingResultEnum,
                                              ausResult2: AutomatedUnderwritingResultEnum,
                                              ausResult3: AutomatedUnderwritingResultEnum,
                                              ausResult4: AutomatedUnderwritingResultEnum,
                                              ausResult5: AutomatedUnderwritingResultEnum
                                            )
