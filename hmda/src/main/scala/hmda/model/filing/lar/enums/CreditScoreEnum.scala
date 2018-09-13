package hmda.model.filing.lar.enums

sealed trait CreditScoreEnum extends LarEnum

object CreditScoreEnum extends LarCodeEnum[CreditScoreEnum] {
  override val values = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  override def valueOf(code: Int): CreditScoreEnum = {
    code match {
      case 1    => EquifaxBeacon5
      case 2    => ExperianFairIsaac
      case 3    => FICORiskScoreClassic04
      case 4    => FICORiskScoreClassic98
      case 5    => VantageScore2
      case 6    => VantageScore3
      case 7    => OneOrMoreCreditScoreModels
      case 8    => OtherCreditScoreModel
      case 9    => CreditScoreNotApplicable
      case 10   => CreditScoreNoCoApplicant
      case 1111 => CreditScoreExempt
      case _    => InvalidCreditScoreCode
    }
  }
}

case object EquifaxBeacon5 extends CreditScoreEnum {
  override val code: Int = 1
  override val description: String = "Equifax Beacon 5.0"
}

case object ExperianFairIsaac extends CreditScoreEnum {
  override val code: Int = 2
  override val description: String = "Experian Fair Isaac"
}

case object FICORiskScoreClassic04 extends CreditScoreEnum {
  override val code: Int = 3
  override val description: String = "FICO Risk Score Classic 04"
}

case object FICORiskScoreClassic98 extends CreditScoreEnum {
  override val code: Int = 4
  override val description: String = "FICO Risk Score Classic 98"
}

case object VantageScore2 extends CreditScoreEnum {
  override val code: Int = 5
  override val description: String = "VantageScore 2.0"
}

case object VantageScore3 extends CreditScoreEnum {
  override val code: Int = 6
  override val description: String = "VantageScore 3.0"
}

case object OneOrMoreCreditScoreModels extends CreditScoreEnum {
  override val code: Int = 7
  override val description: String = "More than one credit scoring model"
}

case object OtherCreditScoreModel extends CreditScoreEnum {
  override val code: Int = 8
  override val description: String = "Other credit scoring model"
}

case object CreditScoreNotApplicable extends CreditScoreEnum {
  override val code: Int = 9
  override val description: String = "Not applicable"
}

case object CreditScoreNoCoApplicant extends CreditScoreEnum {
  override val code: Int = 10
  override val description: String = "No co-applicant"
}

case object CreditScoreExempt extends CreditScoreEnum {
  override def code: Int = 1111
  override def description: String = "Exempt Credit Score"
}

case object InvalidCreditScoreCode extends CreditScoreEnum {
  override def code: Int = -1
  override def description: String = "Invalid Code"
}
