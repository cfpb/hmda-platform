package hmda.js.parser

import hmda.parser.fi.FIDataDatParser
import org.scalajs.dom.Node
import org.scalajs.dom.html._
import org.scalajs.dom.raw.FileReader

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import scalatags.JsDom._
import all._
import scala.language.implicitConversions

@JSExport
object ParserExample {
  @JSExport
  def main(): Unit = {
    println("Parser Example")

    implicit def extendEventTarget(e: dom.EventTarget): EventTargetExt = e.asInstanceOf[EventTargetExt]

    val title = h1("HMDA Parser Example (client side)").render

    addToBody(title)

    val inputFiles: Input = input(
      `type` := "file",
      id := "files",
      name := "files[]"
    ).render

    inputFiles.onchange = { (e: dom.Event) =>
      val file = e.target.files(0)
      println(file.name)

      val reader = new FileReader

      reader.onload = { (e: dom.Event) =>
        val text = reader.result.toString
        val parser = new FIDataDatParser
        val data = parser.read(text.split("\n"))
        println(data.toCSV)
      }

      reader.readAsText(file, "UTF-8")
    }

    addToBody(inputFiles)

  }

  trait EventTargetExt extends dom.EventTarget {
    var files: dom.FileList = js.native
  }

  def addToBody(node: Node): Unit = {
    dom.document.body.appendChild(node)
  }

  def parseFile() = {

  }

}
