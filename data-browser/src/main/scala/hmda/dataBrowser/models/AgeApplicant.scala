package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable

sealed abstract class AgeApplicant(override val entryName: String)
    extends EnumEntry

object AgeApplicant extends Enum[AgeApplicant] {
    val values: immutable.IndexedSeq[AgeApplicant] = findValues

    case object Na                      extends AgeApplicant("8888")
    case object UnderTwentyFive         extends AgeApplicant("<25")
    case object TwentyFiveToThirtyFour  extends AgeApplicant("25-34")
    case object ThirtyFiveToFortyFour   extends AgeApplicant("35-44")
    case object FortyFiveToFiftyFour    extends AgeApplicant("45-54")
    case object FiftyFiveToSixtyFour    extends AgeApplicant("55-64")
    case object SixtyFiveToSeventyFour  extends AgeApplicant("65-74")
    case object SeventyFourAndAbove     extends AgeApplicant(">74")

    def validateAgeApplicant(rawAgeApplicant: Seq[String]): Either[Seq[String], Seq[AgeApplicant]] = {
        val potentialAges =
        rawAgeApplicant.map(age =>
            (age, AgeApplicant.withNameInsensitiveOption(age)))
        val isAgesValid = potentialAges.map(_._2).forall(_.isDefined)

        if (isAgesValid) Right(potentialAges.flatMap(_._2))
        else Left(
            potentialAges.collect {
                case (input, None) => input
            }
        )
    }

}
