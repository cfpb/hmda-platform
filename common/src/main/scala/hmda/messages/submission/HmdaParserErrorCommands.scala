package hmda.messages.submission

import hmda.messages.CommonMessages.Command
import hmda.model.filing.submission.SubmissionId

object HmdaParserErrorCommands {

  sealed trait HmdaParserErrorCommand extends Command

  case class StartParsing(submissionId: SubmissionId) extends Command

}
