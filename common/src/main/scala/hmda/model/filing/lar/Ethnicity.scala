package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{EthnicityEnum, EthnicityObservedEnum, InvalidEthnicityCode, InvalidEthnicityObservedCode}

case class Ethnicity(
    ethnicity1: EthnicityEnum = InvalidEthnicityCode,
    ethnicity2: EthnicityEnum = InvalidEthnicityCode,
    ethnicity3: EthnicityEnum = InvalidEthnicityCode,
    ethnicity4: EthnicityEnum = InvalidEthnicityCode,
    ethnicity5: EthnicityEnum = InvalidEthnicityCode,
    otherHispanicOrLatino: String = "",
    ethnicityObserved: EthnicityObservedEnum = InvalidEthnicityObservedCode
)
