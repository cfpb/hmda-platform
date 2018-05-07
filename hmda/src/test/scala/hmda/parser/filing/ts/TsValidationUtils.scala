package hmda.parser.filing.ts

import hmda.model.filing.ts.TransmittalSheet
import org.scalacheck.Gen

object TsValidationUtils {

  def extractValues(ts: TransmittalSheet): Seq[String] = {
    val id = ts.id.toString
    val institutionName = ts.institutionName
    val year = ts.year.toString
    val quarter = ts.quarter.toString
    val name = ts.contact.name
    val phone = ts.contact.phone
    val email = ts.contact.email
    val street = ts.contact.address.street
    val city = ts.contact.address.city
    val state = ts.contact.address.state
    val zipCode = ts.contact.address.zipCode
    val agencyCode = ts.agency.code.toString
    val totalLines = ts.totalLines.toString
    val taxId = ts.taxId
    val lei = ts.LEI

    List(id,
         institutionName,
         year,
         quarter,
         name,
         phone,
         email,
         street,
         city,
         state,
         zipCode,
         agencyCode,
         totalLines,
         taxId,
         lei)
  }

  def badValue(): String = {
    Gen.alphaStr.sample.getOrElse("a")
  }
}
