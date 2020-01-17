package hmda.model.filing.lar

import hmda.model.filing.lar.enums._
import io.circe._
import io.circe.syntax._

case class Property(
                     propertyValue: String = "",
                     manufacturedHomeSecuredProperty: ManufacturedHomeSecuredPropertyEnum =
                     new InvalidManufacturedHomeSecuredPropertyCode,
                     manufacturedHomeLandPropertyInterest: ManufacturedHomeLandPropertyInterestEnum =
                     new InvalidManufacturedHomeLandPropertyCode,
                     totalUnits: Int = 0,
                     multiFamilyAffordableUnits: String = ""
                   )

object Property {
  implicit val propertyEncoder: Encoder[Property] = (a: Property) =>
    Json.obj(
      ("propertyValue", Json.fromString(a.propertyValue)),
      ("manufacturedHomeSecuredProperty",
        a.manufacturedHomeSecuredProperty.asInstanceOf[LarEnum].asJson),
      ("manufacturedHomeLandPropertyInterest",
        a.manufacturedHomeLandPropertyInterest.asInstanceOf[LarEnum].asJson),
      ("totalUnits", Json.fromInt(a.totalUnits)),
      ("multiFamilyAffordableUnits",
        Json.fromString(value = a.multiFamilyAffordableUnits))
    )

  implicit val propertyDecoder: Decoder[Property] = (c: HCursor) =>
    for {
      propertyValue <- c.downField("propertyValue").as[String]
      manufacturedHomeSecuredProperty <- c
        .downField("manufacturedHomeSecuredProperty")
        .as[Int]
      manufacturedHomeLandPropertyInterest <- c
        .downField("manufacturedHomeLandPropertyInterest")
        .as[Int]
      totalUnits <- c.downField("totalUnits").as[Int]
      multiFamilyAffordableUnits <- c
        .downField("multiFamilyAffordableUnits")
        .as[String]
    } yield
      Property(
        propertyValue,
        ManufacturedHomeSecuredPropertyEnum.valueOf(
          manufacturedHomeSecuredProperty),
        ManufacturedHomeLandPropertyInterestEnum.valueOf(
          manufacturedHomeLandPropertyInterest),
        totalUnits,
        multiFamilyAffordableUnits
      )
}