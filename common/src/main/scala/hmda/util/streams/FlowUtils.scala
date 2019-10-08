package hmda.util.streams

import akka.NotUsed
import akka.stream.scaladsl.{ Flow, Framing }
import akka.util.ByteString

object FlowUtils {

  def framing(delimiter: String): Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString(delimiter), maximumFrameLength = 65536, allowTruncation = true)

}
