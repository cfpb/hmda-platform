package hmda.model.filing.lar.enums

trait LarEnum {
  def code: Int
  def description: String
}

trait LarCodeEnum[+A] {
  val values: List[Int]
  def valueOf(code: Int): A
}
