package hmda.model.filing.lar.enums

sealed trait PurchaserEnum extends LarEnum

object PurchaserEnum extends LarCodeEnum[PurchaserEnum] {
  override val values = List(0, 1, 2, 3, 4, 5, 6, 71, 72, 8, 9)

  override def valueOf(code: Int): PurchaserEnum =
    code match {
      case 0  => PurchaserTypeNotApplicable
      case 1  => FannieMae
      case 2  => GinnieMae
      case 3  => FreddieMac
      case 4  => FarmerMac
      case 5  => PrivateSecuritizer
      case 6  => Bank
      case 71 => CreditUnion
      case 72 => LifeInsuranceCompany
      case 8  => AffiliateInstitution
      case 9  => OtherPurchaserType
      case other  => new InvalidPurchaserCode(other)
    }
}

case object PurchaserTypeNotApplicable extends PurchaserEnum {
  override val code: Int           = 0
  override val description: String = "Not Applicable"
}

case object FannieMae extends PurchaserEnum {
  override val code: Int           = 1
  override val description: String = "Fannie Mae"
}

case object GinnieMae extends PurchaserEnum {
  override val code: Int           = 2
  override val description: String = "Ginnie Mae"
}

case object FreddieMac extends PurchaserEnum {
  override val code: Int           = 3
  override val description: String = "Freddie Mac"
}

case object FarmerMac extends PurchaserEnum {
  override val code: Int           = 4
  override val description: String = "Farmer Mac"
}

case object PrivateSecuritizer extends PurchaserEnum {
  override val code: Int           = 5
  override val description: String = "Private securitizer"
}

case object Bank extends PurchaserEnum {
  override val code: Int = 6
  override val description: String =
    "Commercial bank, savings bank, or savings association"
}

case object CreditUnion extends PurchaserEnum {
  override val code: Int = 71
  override val description: String =
    "Credit union, mortgage company, or finance company"
}

case object LifeInsuranceCompany extends PurchaserEnum {
  override val code: Int           = 72
  override val description: String = "Life insurance company"
}

case object AffiliateInstitution extends PurchaserEnum {
  override val code: Int           = 8
  override val description: String = "Affiliate institution"
}

case object OtherPurchaserType extends PurchaserEnum {
  override val code: Int           = 9
  override val description: String = "Other type of purchaser"
}

case object InvalidPurchaserExemptCode extends PurchaserEnum {
  override val code: Int = 1111
  override val description: String =
    "Invalid exemption code for loan field."
}


class InvalidPurchaserCode(value: Int = -1) extends PurchaserEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidPurchaserCode => true
            case _ => false
        }
}
