package hmda.model.filing.lar

import hmda.model.filing.lar.enums._
import io.circe._
import io.circe.syntax._

case class LarAction(
                      preapproval: PreapprovalEnum = new InvalidPreapprovalCode,
                      actionTakenType: ActionTakenTypeEnum = new InvalidActionTakenTypeCode,
                      actionTakenDate: Int = 0
                    )

object LarAction {
  implicit val larActionEncoder: Encoder[LarAction] = (a: LarAction) =>
    Json.obj(
      ("preapproval", a.preapproval.asInstanceOf[LarEnum].asJson),
      ("actionTakenType", a.actionTakenType.asInstanceOf[LarEnum].asJson),
      ("actionTakenDate", Json.fromInt(a.actionTakenDate))
    )

  implicit val larActionDecoder: Decoder[LarAction] = (c: HCursor) =>
    for {
      preapproval <- c.downField("preapproval").as[Int]
      actionTakenType <- c.downField("actionTakenType").as[Int]
      actionTakenDate <- c.downField("actionTakenDate").as[Int]
    } yield
      LarAction(
        PreapprovalEnum.valueOf(preapproval),
        ActionTakenTypeEnum.valueOf(actionTakenType),
        actionTakenDate
      )
}