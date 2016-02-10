package hmda.js.parser

import hmda.parser.fi.FIDataDatParser
import org.scalajs.dom
import org.scalajs.dom.Node
import org.scalajs.dom.html._
import org.scalajs.dom.raw.FileReader

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom._
import scalatags.JsDom.all._

@JSExport
object ParserExample {
  @JSExport
  def main(): Unit = {

    implicit def extendEventTarget(e: dom.EventTarget): EventTargetExt = e.asInstanceOf[EventTargetExt]

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

    val content = div(`class` := "container")(
      h1("HMDA Parser Example (Scala.js)"),
      inputFiles,
      h2("Transmittal Sheet"),
      table(
        `class` := "table",
        thead(
          tr(
            th("Activity Year"),
            th("Respondent Name"),
            th("Contact Email")
          ),
          tr(
            th(
              id := "activityYear"
            ),
            th(
              id := "respondentName"
            ),
            th(
              id := "contactEmail"
            )
          )
        )
      ),
      h2("Loan Application Register"),
      table(
        `class` := "table",
        id := "larTable",
        thead(
          tr(
            th("Agency Code"),
            th("Respondent ID"),
            th("Action Taken Date"),
            th("Loan Application Date")
          )
        )
      )
    ).render

    addToBody(content)

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
