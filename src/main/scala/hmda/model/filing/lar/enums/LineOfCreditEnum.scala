package hmda.model.filing.lar.enums

sealed trait LineOfCreditEnum {
  val code: Int
  val description: String
}

object LineOfCreditEnum {
  val values = List(1, 2)

  def valueOf(code: Int): LineOfCreditEnum = {
    code match {
      case 1 => OpenEndLineOfCredit
      case 2 => NotOpenEndLineOfCredit
      case _ => throw new Exception("Invalid Line of Credit Code")
    }
  }
}

case object OpenEndLineOfCredit extends LineOfCreditEnum {
  override val code: Int = 1
  override val description: String = "Open-end line of credit"
}

case object NotOpenEndLineOfCredit extends LineOfCreditEnum {
  override val code: Int = 2
  override val description: String = "Not an open-end line of credit"
}
