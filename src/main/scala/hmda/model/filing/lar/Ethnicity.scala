package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{EthnicityEnum, EthnicityObservedEnum}

case class Ethnicity(
    ethnicity1: EthnicityEnum,
    ethnicity2: EthnicityEnum,
    ethnicity3: EthnicityEnum,
    ethnicity4: EthnicityEnum,
    ethnicity5: EthnicityEnum,
    otherHispanicOrLatino: String,
    ethnicityObserved: EthnicityObservedEnum
)
