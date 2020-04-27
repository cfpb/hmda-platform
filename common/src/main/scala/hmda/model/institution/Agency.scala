package hmda.model.institution

import io.circe._

sealed trait Agency {
  val code: Int

  val name: String
  val fullName: String
}

object Agency {
  val values = List(1, 2, 3, 5, 7, 9, -1)

  def apply(): Agency = CFPB

  def valueOf(code: Int): Agency =
    code match {
      case 1  => OCC
      case 2  => FRS
      case 3  => FDIC
      case 5  => NCUA
      case 7  => HUD
      case 9  => CFPB
      case -1 => UnknownAgency
      case invalidCode =>  throw new Exception("Invalid Agency Code: "+ invalidCode)
    }

  implicit val agencyEncoder: Encoder[Agency] = (a: Agency) =>
    Json.obj(
      ("agency", Json.fromInt(a.code))
    )

  implicit val agencyDecoder: Decoder[Agency] = (c: HCursor) =>
    for {
      code <- c.downField("agency").as[Int]
    } yield {
      Agency.valueOf(code)
    }
}

case object OCC extends Agency {
  override val code = 1

  override val name     = "occ"
  override val fullName = "Office of the Comptroller of the Currency (OCC)"
}

case object FRS extends Agency {
  override val code = 2

  override val name     = "frs"
  override val fullName = "Federal Reserve System (FRS)"
}

case object FDIC extends Agency {
  override val code = 3

  override val name     = "fdic"
  override val fullName = "Federal Deposit Insurance Corporation (FDIC)"
}

case object NCUA extends Agency {
  override val code = 5

  override val name     = "ncua"
  override val fullName = "National Credit Union Administration (NCUA)"
}

case object HUD extends Agency {
  override val code = 7

  override val name     = "hud"
  override val fullName = "Housing and Urban Development (HUD)"
}

case object CFPB extends Agency {
  override val code = 9

  override val name     = "cfpb"
  override val fullName = "Consumer Financial Protection Bureau (CFPB)"
}

case object UnknownAgency extends Agency {
  override val code = -1
  override val name     = "unknown"
  override val fullName = "Unknown Agency"
}
