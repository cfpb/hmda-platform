package hmda.model.filing.ts

import hmda.model.filing.PipeDelimited
import io.circe.Codec
import io.circe.generic.semiauto._

case class Contact(
                    name: String = "",
                    phone: String = "",
                    email: String = "",
                    address: Address = Address()
                  ) extends PipeDelimited {
  override def toCSV: String = {
    s"$name|$phone|$email|${address.toCSV}"
  }
}
object Contact {
  implicit val codec: Codec[Contact] = deriveCodec[Contact]
}