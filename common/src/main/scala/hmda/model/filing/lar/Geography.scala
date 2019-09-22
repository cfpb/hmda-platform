package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import io.circe._

case class Geography(street: String = "",
                     city: String = "",
                     state: String = "",
                     zipCode: String = "",
                     county: String = "",
                     tract: String = "")
  extends PipeDelimited {
  override def toCSV: String = {
    s"$street|$city|$state|$zipCode|$county|$tract"
  }
}

object Geography {
  implicit val geographyEncoder: Encoder[Geography] = (a: Geography) =>
    Json.obj(
      ("street", Json.fromString(a.street)),
      ("city", Json.fromString(a.city)),
      ("state", Json.fromString(a.state)),
      ("zipCode", Json.fromString(a.zipCode)),
      ("county", Json.fromString(a.county)),
      ("tract", Json.fromString(a.tract))
    )

  implicit val geographyDecoder: Decoder[Geography] = (c: HCursor) =>
    for {
      street <- c.downField("street").as[String]
      city <- c.downField("city").as[String]
      state <- c.downField("state").as[String]
      zipCode <- c.downField("zipCode").as[String]
      county <- c.downField("county").as[String]
      tract <- c.downField("tract").as[String]
    } yield Geography(street, city, state, zipCode, county, tract)

}