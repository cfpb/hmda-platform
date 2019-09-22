package hmda.model.filing.submission

import hmda.model.filing.submission.SubmissionStatusMessages._

object SubmissionStatus {

  def valueOf(code: Int): SubmissionStatus = code match {
    case 1  => Created
    case 2  => Uploading
    case 3  => Uploaded
    case 4  => Parsing
    case 5  => ParsedWithErrors
    case 6  => Parsed
    case 7  => Validating
    case 8  => SyntacticalOrValidity
    case 9  => SyntacticalOrValidityErrors
    case 10 => Quality
    case 11 => QualityErrors
    case 12 => Macro
    case 13 => MacroErrors
    case 14 => Verified
    case 15 => Signed
    case -1 => Failed
  }

}

sealed trait SubmissionStatus {
  def code: Int
  def message: String
  def description: String
  def qualityVerified: Boolean
  def macroVerified: Boolean
}

case object Created extends SubmissionStatus {
  override def code: Int = 1
  override def message: String = createdMsg
  override def description: String = createdDescription
  override def qualityVerified = false
  override def macroVerified = false
}

case object Uploading extends SubmissionStatus {
  override def code: Int = 2
  override def message: String = uploadingMsg
  override def description: String = uploadingDescription
  override def qualityVerified = false
  override def macroVerified = false
}

case object Uploaded extends SubmissionStatus {
  override def code: Int = 3
  override def message: String = uploadedMsg
  override def description: String = uploadedDescription
  override def qualityVerified = false
  override def macroVerified = false
}

case object Parsing extends SubmissionStatus {
  override def code: Int = 4
  override def message: String = parsingMsg
  override def description: String = parsingDescription
  override def qualityVerified = false
  override def macroVerified = false
}

case object ParsedWithErrors extends SubmissionStatus {
  override def code: Int = 5
  override def message: String = parsedWithErrorsMsg
  override def description: String = parsedWithErrorsDescription
  override def qualityVerified = false
  override def macroVerified = false
}

case object Parsed extends SubmissionStatus {
  override def code: Int = 6
  override def message: String = parsedMsg
  override def description: String = parsedDescription
  override def qualityVerified = false
  override def macroVerified = false
}

case object Validating extends SubmissionStatus {
  override def code: Int = 7
  override def message: String = validatingMsg
  override def description: String = validatingDescription
  override def qualityVerified = false
  override def macroVerified = false
}

case object SyntacticalOrValidity extends SubmissionStatus {
  override def code: Int = 8
  override def message: String = syntacticalOrValidityMsg
  override def description: String = syntacticalOrValidityDescription
  override def qualityVerified = false
  override def macroVerified = false
}

case object SyntacticalOrValidityErrors extends SubmissionStatus {
  override def code: Int = 9
  override def message: String = syntacticalValidityErrorMsg
  override def description: String = syntactivalValidityErrorDescription
  override def qualityVerified = false
  override def macroVerified = false
}

case object Quality extends SubmissionStatus {
  override def code: Int = 10
  override def message: String = qualityMsg
  override def description: String = qualityDescription
  override def qualityVerified = true
  override def macroVerified = false
}

case object QualityErrors extends SubmissionStatus {
  override def code: Int = 11
  override def message: String = qualityErrorMsg
  override def description: String = qualityErrorDescription
  override def qualityVerified = true
  override def macroVerified = false
}

case object Macro extends SubmissionStatus {
  override def code: Int = 12
  override def message: String = macroMsg
  override def description: String = macroDescription
  override def qualityVerified = true
  override def macroVerified = true
}

case object MacroErrors extends SubmissionStatus {
  override def code: Int = 13
  override def message: String = macroErrorMsg
  override def description: String = macroErrorDescription
  override def qualityVerified = true
  override def macroVerified = true
}

case object Verified extends SubmissionStatus {
  override def code: Int = 14
  override def message: String = validatedMsg
  override def description: String = validatedDescription
  override def qualityVerified = true
  override def macroVerified = true
}

case object Signed extends SubmissionStatus {
  override def code: Int = 15
  override def message: String = signedMsg
  override def description: String = signedDescription
  override def qualityVerified = true
  override def macroVerified = true
}

case object Failed extends SubmissionStatus {
  override def code: Int = -1
  override def message: String = failedMsg
  override def description: String = failedDescription
  override def qualityVerified = false
  override def macroVerified = false
}