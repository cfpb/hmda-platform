package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{
  ManufacturedHomeLandPropertyInterestEnum,
  ManufacturedHomeSecuredPropertyEnum
}

case class Property(
    propertyValue: String,
    manufacturedHomeSecuredProperty: ManufacturedHomeSecuredPropertyEnum,
    manufacturedHomeLandPropertyInterest: ManufacturedHomeLandPropertyInterestEnum,
    totalUnits: Int,
    multiFamilyAffordableUnits: String
)
