package hmda.js.io

import scala.concurrent.{ Promise, Future }
import scala.scalajs.js
import scala.scalajs.js.Dynamic._

trait IO {
  val fs = global.require("fs")
  val options = js.Dynamic.literal(encoding = "UTF-8")

  def readFileSync(path: String): String = {
    fs.readFileSync(path, options).asInstanceOf[String]
  }

  def readFile(path: String): Future[String] = {
    val promise: Promise[String] = Promise()
    fs.readFile(path, options, { (err: js.Dynamic, data: String) =>
      if (err != null)
        promise.failure(new RuntimeException("could not read file"))
      else
        promise.success(data)
    })
    promise.future
  }
}
