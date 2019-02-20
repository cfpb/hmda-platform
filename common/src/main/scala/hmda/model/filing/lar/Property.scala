package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{InvalidManufacturedHomeLandPropertyCode, InvalidManufacturedHomeSecuredPropertyCode, ManufacturedHomeLandPropertyInterestEnum, ManufacturedHomeSecuredPropertyEnum}

case class Property(
    propertyValue: String = "",
    manufacturedHomeSecuredProperty: ManufacturedHomeSecuredPropertyEnum =
      InvalidManufacturedHomeSecuredPropertyCode,
    manufacturedHomeLandPropertyInterest: ManufacturedHomeLandPropertyInterestEnum =
      InvalidManufacturedHomeLandPropertyCode,
    totalUnits: Int = 0,
    multiFamilyAffordableUnits: String = ""
)
