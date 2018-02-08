package hmda.model.filing.lar

import hmda.model.filing.lar.enums.AutomatedUnderwritingSystemEnum

case class AutomatedUnderwritingSystem(
                                        aus1: AutomatedUnderwritingSystemEnum,
                                        aus2: AutomatedUnderwritingSystemEnum,
                                        aus3: AutomatedUnderwritingSystemEnum,
                                        aus4: AutomatedUnderwritingSystemEnum,
                                        aus5: AutomatedUnderwritingSystemEnum
                                      )
