package hmda.model.filing.ts

import hmda.model.filing.PipeDelimited
import io.circe.Codec
import io.circe.generic.semiauto._

case class Address(
  street: String = "",
  city: String = "",
  state: String = "",
  zipCode: String = ""
) extends PipeDelimited {
  override def toCSV: String =
    s"$street|$city|$state|$zipCode"
}

object Address {
  implicit val codec: Codec[Address] = deriveCodec[Address]
}
