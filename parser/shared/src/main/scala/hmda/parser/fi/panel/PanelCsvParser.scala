package hmda.parser.fi.panel

import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

import scala.io.Source
import scala.util.Try

object PanelCsvParser extends App {
  val file = pickFile()
  val lines = file.getLines().toList.tail

  for (line <- lines) {
    val i = InstitutionParser(line)
    println(i)
  }

  def pickFile(): Source = {
    val chooser = new JFileChooser()
    chooser.setCurrentDirectory(new java.io.File("."))
    chooser.setDialogTitle("Input File")
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
    chooser.setFileFilter(new FileNameExtensionFilter("csv", "csv"))

    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION &&
      Try(Source.fromFile(chooser.getSelectedFile)).isSuccess) {
      Source.fromFile(chooser.getSelectedFile)
    } else {
      println("\nWARNING: Unable to read file input.")
      Source.fromFile(".")
    }
  }
}
