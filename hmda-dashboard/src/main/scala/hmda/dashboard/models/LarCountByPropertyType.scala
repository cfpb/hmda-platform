package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class LarCountByPropertyType(
                                   single_family: Int,
                      manufactured_single_family: Int,
                      multifamily: Int
                    )

object LarCountByPropertyType {
  implicit val getResults: GetResult[LarCountByPropertyType] = GetResult(r => LarCountByPropertyType(r.<<,r.<<,r.<<))

  implicit val codec: Codec[LarCountByPropertyType] =
    Codec.forProduct3("Single Family","Manufactured Single Family","Multifamily")(LarCountByPropertyType.apply)(f => (f.single_family,f.manufactured_single_family,f.multifamily))
}
