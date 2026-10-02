package hmda.publisher.helper

trait TSHeader {

  val TSPublicHeader = "activity_year|calendar_quarter|lei|tax_id|agency_code|respondent_name|respondent_state|respondent_city|respondent_zip_code|lar_count" + "\n"
  val TSPublicHeaderCSV = "activity_year,calendar_quarter,lei,tax_id,agency_code,respondent_name,respondent_state,respondent_city,respondent_zip_code,lar_count" + "\n"

  val TSPrivateHeader = "id|institution_name|year|quarter|name|phone|email|street|city|state|zip_code|agency|total_lines|tax_id|lei|latest_sign_date|first_sign_date" + "\n"
  val TSPrivateHeaderCSV = "id,institution_name,year,quarter,name,phone,email,street,city,state,zip_code,agency,total_lines,tax_id,lei,latest_sign_date,first_sign_date" + "\n"
}

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
