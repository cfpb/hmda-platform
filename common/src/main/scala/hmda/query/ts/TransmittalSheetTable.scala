package hmda.query.ts

import java.sql.Timestamp
import hmda.query.DbConfiguration._
import dbConfig.profile.api._

class TransmittalSheetTable(tag: Tag, tableName: String) extends Table[TransmittalSheetEntity](tag, tableName) {

  def lei = column[String]("lei", O.PrimaryKey)
  def id = column[Int]("id")
  def institutionName = column[String]("institution_name")
  def year = column[Int]("year")
  def quarter = column[Int]("quarter")
  def name = column[String]("name")
  def phone = column[String]("phone")
  def email = column[String]("email")
  def street = column[String]("street")
  def city = column[String]("city")
  def state = column[String]("state")
  def zipCode = column[String]("zip_code")
  def agency = column[Int]("agency")
  def totalLines = column[Int]("total_lines")
  def taxId = column[String]("tax_id")
  def submissionId = column[Option[String]]("submission_id")
  def createdAt = column[Option[Timestamp]]("created_at")
  def isQuarterly = column[Option[Boolean]]("is_quarterly")
  def signDate = column[Option[Long]]("sign_date")

  override def * =
    (
      lei,
      id,
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
      agency,
      totalLines,
      taxId,
      submissionId,
      createdAt,
      isQuarterly,
      signDate
    ) <> ((TransmittalSheetEntity.apply _).tupled, TransmittalSheetEntity.unapply)
}