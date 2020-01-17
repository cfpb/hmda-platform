package hmda.model.filing.lar

import hmda.model.filing.lar.enums._
import io.circe._
import io.circe.syntax._

case class Sex(sexEnum: SexEnum = new InvalidSexCode,
               sexObservedEnum: SexObservedEnum = new InvalidSexObservedCode)

object Sex {
  implicit val sexEncoder: Encoder[Sex] = (a: Sex) =>
    Json.obj(
      ("sex", a.sexEnum.asInstanceOf[LarEnum].asJson),
      ("sexObserved", a.sexObservedEnum.asInstanceOf[LarEnum].asJson)
    )

  implicit val sexDecoder: Decoder[Sex] = (c: HCursor) =>
    for {
      sex <- c.downField("sex").as[Int]
      sexObserved <- c.downField("sexObserved").as[Int]
    } yield Sex(SexEnum.valueOf(sex), SexObservedEnum.valueOf(sexObserved))

}