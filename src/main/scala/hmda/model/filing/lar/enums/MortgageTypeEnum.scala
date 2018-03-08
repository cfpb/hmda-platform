package hmda.model.filing.lar.enums

sealed trait MortgageTypeEnum extends LarEnum

object MortgageTypeEnum extends LarCodeEnum[MortgageTypeEnum] {
  override val values = List(1, 2)

  override def valueOf(code: Int): MortgageTypeEnum = {
    code match {
      case 1 => ReverseMortgate
      case 2 => NotReverseMortgage
      case _ => throw new Exception("Invalid Mortgage Type Code")
    }
  }
}

case object ReverseMortgate extends MortgageTypeEnum {
  override val code: Int = 1
  override val description: String = "Reverse mortgage"
}

case object NotReverseMortgage extends MortgageTypeEnum {
  override val code: Int = 2
  override val description: String = "Not a reverse mortgate"
}
