package hmda.util.streams

import pekko.NotUsed
import pekko.stream.scaladsl.{ Flow, Framing }
import pekko.util.ByteString

object FlowUtils {

  def framing(delimiter: String): Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString(delimiter), maximumFrameLength = 65536, allowTruncation = true)

}
