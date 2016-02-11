package hmda.js.parser

import hmda.model.fi.FIData
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
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
        val ts = data.ts
        addTsToTable(ts)
        val lars = data.lars
        addLarsToTable(lars)
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

  def addTsToTable(ts: TransmittalSheet): Unit = {
    val document = dom.document
    document.getElementById("activityYear").innerHTML = ts.activityYear.toString
    document.getElementById("respondentName").innerHTML = ts.respondent.name
    document.getElementById("contactEmail").innerHTML = ts.contact.email
  }

  def addLarsToTable(lars: Iterator[LoanApplicationRegister]): Unit = {
    val larTable = dom.document.getElementById("larTable")
    val document = dom.document
    lars.foreach { lar =>
      val row = document.createElement("tr")
      val codeCell = document.createElement("th")
      val actionTakenDateCell = document.createElement("th")
      val respondentIdCell = document.createElement("th")
      val loanApplicationDateCell = document.createElement("th")
      val codeValue = document.createTextNode(lar.agencyCode.toString)
      val actionTakenDateValue = document.createTextNode(lar.actionTakenDate.toString)
      val respondentIdValue = document.createTextNode(lar.respondentId)
      val loanApplicationDateValue = document.createTextNode(lar.loan.applicationDate)

      codeCell.appendChild(codeValue)
      actionTakenDateCell.appendChild(actionTakenDateValue)
      respondentIdCell.appendChild(respondentIdValue)
      loanApplicationDateCell.appendChild(loanApplicationDateValue)
      row.appendChild(codeCell)
      row.appendChild(actionTakenDateCell)
      row.appendChild(respondentIdCell)
      row.appendChild(loanApplicationDateCell)
      larTable.appendChild(row)
    }
  }

}
