package hmda.model.messages

import java.time.Instant

case class ProcessingStatus(id: String = "", dateStarted: String = Instant.now.toString, rowsUploaded: Int = 0)

case class ProcessingStatusSeq(uploads: Seq[ProcessingStatus] = Nil)
