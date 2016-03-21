package util

import java.io.File

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, IOResult }
import akka.stream.scaladsl._
import akka.util.ByteString
import hmda.parser.fi.lar.LarDatParser
import hmda.parser.fi.ts.TsDatParser

import scala.concurrent.ExecutionContext

/*
Takes a .DAT file and converts to pipe delimited CSV (2017)
 */
object Dat2Csv {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      throw new Exception("Please provide input .DAT and output .txt files")
    } else {
      val datFilePath = args(0)
      val txtFilePath = args(1)

      val datFile = new File(datFilePath)
      val txtFile = new File(txtFilePath)

      val source = FileIO.fromFile(datFile)
      val sink = FileIO.toFile(txtFile)

      val framing = Framing.delimiter(ByteString("\n"), 2048, allowTruncation = true)

      val dat2CsvFlow: Flow[String, String, NotUsed] = {
        Flow[String]
          .map { s =>
            if (s.charAt(0).toString == "1") {
              TsDatParser(s).toCSV
            } else {
              LarDatParser(s).toCSV
            }
          }
      }

      val convert = source
        .via(framing)
        .map(_.utf8String)
        .via(dat2CsvFlow)
        .map(s => ByteString(s"$s\n"))
        .runWith(sink)

    }
  }

}
