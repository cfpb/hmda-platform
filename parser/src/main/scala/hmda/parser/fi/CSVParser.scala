package hmda.parser.fi

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

import scala.io.Source
import scala.util.Try

import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser

object CSVParser extends App {
  val file = Try(Source.fromFile(new File(args(0))))
    .getOrElse(Source.fromFile(new File(getClass.getResource("/testClean.txt").getPath)))
  val lines = file.getLines.toList
  var error = false

  val tsErrors = TsCsvParser(lines.head) match {
    case Right(ts) => List()
    case Left(tsErrorList) => tsErrorList
  }
  if (tsErrors.nonEmpty) {
    println("TS Errors:")
    for (error <- tsErrors) {
      println("\t" + error)
    }
    error = true
  }

  for (line <- lines.tail) {
    val larErrors = LarCsvParser(line) match {
      case Right(lar) => List()
      case Left(larErrorList) => larErrorList
    }
    if (larErrors.nonEmpty) {
      error = true
      println("Errors on LAR #" + (lines.indexOf(line) + 1))
      for (error <- larErrors) {
        println("\t" + error)
      }
    }
  }

  if (!error) {
    for (line <- Source.fromFile(new File(getClass.getResource("/Success.txt").getPath)).getLines) {
      println(line)
    }
  }

  def pickFile(): Source = {
    val chooser = new JFileChooser()
    chooser.setCurrentDirectory(new java.io.File("."))
    chooser.setDialogTitle("Input File")
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
    chooser.setFileFilter(new FileNameExtensionFilter("txt", "txt"))

    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION &&
      Try(Source.fromFile(chooser.getSelectedFile)).isSuccess) {
      Source.fromFile(chooser.getSelectedFile)
    } else {
      println("\nWARNING: Unable to read file input.")
      Source.fromFile(".")
    }
  }
}
