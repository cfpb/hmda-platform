package hmda.model.fi.ts

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait NameAndAddress {
  def name: String
  def address: String
  def city: String
  def state: String
  def zipCode: String
}
