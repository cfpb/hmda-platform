package hmda.model.filing.lar.enums

import io.circe.{Encoder, Json}

trait LarEnum {
  def code: Int
  def description: String
}

object LarEnum {
  implicit val enumEncoder: Encoder[LarEnum] = (a: LarEnum) =>
    Json.fromInt(a.code)
}

trait LarCodeEnum[+A] {
  val values: List[Int]
  def valueOf(code: Int): A
}