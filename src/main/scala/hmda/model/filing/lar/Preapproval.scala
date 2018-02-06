package hmda.model.filing.lar

sealed trait Preapproval {
  val code: Int
  val description: String
}

object Preapproval {
  val values = List(1, 2)

  def valueOf(code: Int): Preapproval = {
    code match {
      case 1 => PreapprovalRequested
      case 2 => PreapprovalNotRequested
      case _ => throw new Exception("Invalid Preapproval Code")
    }
  }
}

case object PreapprovalRequested extends Preapproval {
  override val code: Int = 1
  override val description: String = "Preapproval requested"
}

case object PreapprovalNotRequested extends Preapproval {
  override val code: Int = 2
  override val description: String = "Preapproval not requested"
}
