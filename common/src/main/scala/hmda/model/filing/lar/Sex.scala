package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{InvalidSexCode, InvalidSexObservedCode, SexEnum, SexObservedEnum}

case class Sex(sexEnum: SexEnum = InvalidSexCode,
               sexObservedEnum: SexObservedEnum = InvalidSexObservedCode)
