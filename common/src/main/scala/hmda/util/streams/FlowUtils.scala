package hmda.util.streams

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{ Flow, Framing }
import org.apache.pekko.util.ByteString

object FlowUtils {

  def framing(delimiter: String): Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString(delimiter), maximumFrameLength = 65536, allowTruncation = true)

}
