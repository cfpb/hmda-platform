package hmda.publisher.helper

trait TSHeader {

  val TSPublicHeader = "activity_year|calendar_quarter|lei|tax_id|agency_code|respondent_name|respondent_state|respondent_city|respondent_zip_code|lar_count" + "\n"
  val TSPublicHeaderCSV = "activity_year,calendar_quarter,lei,tax_id,agency_code,respondent_name,respondent_state,respondent_city,respondent_zip_code,lar_count" + "\n"

  val TSPrivateHeader = "record_identifier|respondent_name|calendar_year|calendar_quarter|contact_name|contact_phone|contact_email|contact_street|contact_city|contact_state|contact_zip_code|agency_code|lar_count|tax_id|lei|latest_sign_date|first_sign_date" + "\n"
  val TSPrivateHeaderCSV = "record_identifier,respondent_name,calendar_year,calendar_quarter,contact_name,contact_phone,contact_email,contact_street,contact_city,contact_state,contact_zip_code,agency_code,lar_count,tax_id,lei,latest_sign_date,first_sign_date" + "\n"}

object TsPublicHeaderObj extends TSHeader {
  def getTSPublicHeader = {
    TSPublicHeader
  }
  def getTSPublicHeaderCSV: String = {
    TSPublicHeaderCSV
  }
  def getTSPrivateHeader = {
    TSPrivateHeader
  }
  def getTSPrivateHeaderCSV: String = {
    TSPrivateHeaderCSV
  }
}
