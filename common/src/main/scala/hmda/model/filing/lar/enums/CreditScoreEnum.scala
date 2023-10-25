package hmda.model.filing.lar.enums

sealed trait CreditScoreEnum extends LarEnum

object CreditScoreEnum extends LarCodeEnum[CreditScoreEnum] {
  override val values = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,12,13,14,15, 1111)

  override def valueOf(code: Int): CreditScoreEnum =
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
      case 11   => FICOScore9
      case 12 => FICOScore8
      case 13 => FICOScore10
      case 14 => FICOScore10T
      case 15 => VantageScore4
      case 1111 => CreditScoreExempt
      case other    => new InvalidCreditScoreCode(other)
    }
}

case object EquifaxBeacon5 extends CreditScoreEnum {
  override val code: Int           = 1
  override val description: String = "Equifax Beacon 5.0"
}

case object ExperianFairIsaac extends CreditScoreEnum {
  override val code: Int           = 2
  override val description: String = "Experian Fair Isaac"
}

case object FICORiskScoreClassic04 extends CreditScoreEnum {
  override val code: Int           = 3
  override val description: String = "FICO Risk Score Classic 04"
}

case object FICORiskScoreClassic98 extends CreditScoreEnum {
  override val code: Int           = 4
  override val description: String = "FICO Risk Score Classic 98"
}

case object VantageScore2 extends CreditScoreEnum {
  override val code: Int           = 5
  override val description: String = "VantageScore 2.0"
}

case object VantageScore3 extends CreditScoreEnum {
  override val code: Int           = 6
  override val description: String = "VantageScore 3.0"
}

case object OneOrMoreCreditScoreModels extends CreditScoreEnum {
  override val code: Int           = 7
  override val description: String = "More than one credit scoring model"
}

case object OtherCreditScoreModel extends CreditScoreEnum {
  override val code: Int           = 8
  override val description: String = "Other credit scoring model"
}

case object CreditScoreNotApplicable extends CreditScoreEnum {
  override val code: Int           = 9
  override val description: String = "Not applicable"
}

case object CreditScoreNoCoApplicant extends CreditScoreEnum {
  override val code: Int           = 10
  override val description: String = "No co-applicant"
}

case object FICOScore9 extends CreditScoreEnum {
  override def code: Int           = 11
  override def description: String = "FICO Score 9"
}

case object FICOScore8 extends CreditScoreEnum {
  override def code: Int           = 12
  override def description: String = "FICO Score 8"
}

case object FICOScore10 extends CreditScoreEnum {
  override def code: Int           = 13
  override def description: String = "FICO Score 10"
}

case object FICOScore10T extends CreditScoreEnum {
  override def code: Int           = 14
  override def description: String = "FICO Score 10T"
}

case object VantageScore4 extends CreditScoreEnum {
  override def code: Int           = 15
  override def description: String = "Vantage Score 4.0"
}
case object CreditScoreExempt extends CreditScoreEnum {
  override def code: Int           = 1111
  override def description: String = "Exempt Credit Score"
}

class InvalidCreditScoreCode(value: Int = -1) extends CreditScoreEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidCreditScoreCode => true
            case _ => false
        }
}
