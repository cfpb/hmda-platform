package hmda.query.repository

import hmda.model.disclosure.{LoanType, TractDisclosure}
import hmda.model.institution.MsaMd
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile, SetParameter}

import scala.concurrent.{ExecutionContext, Future}

class DisclosureRepository(tableName: String,
                           databaseConfig: DatabaseConfig[JdbcProfile]) {
  import databaseConfig.profile.api._
  implicit val getMsaMdResult: AnyRef with GetResult[MsaMd] = GetResult(
    r => MsaMd(r.<<, r.<<))
  implicit val getLoanTypeResult: AnyRef with GetResult[LoanType] = GetResult(
    r => LoanType(r.<<, r.<<, r.<<))

  implicit val ec = ExecutionContext.global
  private val db = databaseConfig.db

  def actionsTakenTable1: Map[String, String] = {
    Map(
      "Applications Received" -> "1,2,3,4,5",
      "Loans Originated" -> "1",
      "Applications Approved but not Accepted" -> "2",
      "Applications Denied by Financial Institution" -> "3",
      "Applications Withdrawn by Applicant" -> "4",
      "File Closed for Incompleteness" -> "5"
    )
  }

  def actionsTakenTable2: Map[String, String] = {
    Map(
      "Purchased Loans" -> "6"
    )
  }

  /**
    * Return all tracts for an MSA and LEI
    * @param lei
    * @param msaMd
    * @return
    */
  def tractsForMsaMd(lei: String,
                     msaMd: Int,
                     filingYear: Int): Future[Vector[String]] = {
    db.run {
      sql"""select distinct(tract) from modifiedlar2018
            where lei = ${lei.toUpperCase}
            and msa_md = ${msaMd}
            and filing_year = ${filingYear}""".as[String]
    }
  }

  /**
    * FHA, FSA/RHS & VA (A): Total Units = 1 through 4; Purpose of Loan = 1; Loan Type = 2, 3, 4
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionA(lei: String,
                   msaMd: Int,
                   tractToMsaMd: String,
                   filingYear: Int,
                   actionType: String): Future[LoanType] = {

    db.run {
      sql"""select 'FHA, FSA/RHS & VA (A)' as disposition_name, count(*), sum(loan_amount) from modifiedlar2018
               where UPPER(lei) = ${lei.toUpperCase}
               and action_taken_type in (#${actionType})
               and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
               and loan_purpose = 1
               and (loan_type = 2 or loan_type = 3 or loan_type = 4)
               and msa_md = ${msaMd}
               and tract_to_msamd = ${tractToMsaMd}
               and filing_year = ${filingYear}
               group by lei"""
        .as[LoanType]
        .headOption
        .map(_.getOrElse(LoanType("FHA, FSA/RHS & VA (A)", 0, 0)))
    }
  }

  /**
    * Conventional (B): Total Units = 1 through 4; Purpose of Loan = 1; Loan Type = 1
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionB(lei: String,
                   msaMd: Int,
                   tractToMsaMd: String,
                   filingYear: Int,
                   actionType: String): Future[LoanType] = {
    db.run {
      sql"""select 'Conventional (B)',count(*), sum(loan_amount) from modifiedlar2018
           where lei = ${lei.toUpperCase}
           and action_taken_type in (#${actionType})
           and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
           and loan_purpose = 1
           and loan_type = 1
           and msa_md = ${msaMd}
           and tract_to_msamd = ${tractToMsaMd}
           and filing_year = ${filingYear}
           group by lei"""
        .as[LoanType]
        .headOption
        .map(_.getOrElse(LoanType("Conventional (B)", 0, 0)))
    }
  }

  /**
    * Refinancings (C): Total Units = 1 through 4; Purpose of Loan = 31, 32
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionC(lei: String,
                   msaMd: Int,
                   tractToMsaMd: String,
                   filingYear: Int,
                   actionType: String): Future[LoanType] = {
    db.run {
      sql"""select 'Refinancings (C)',count(*), sum(loan_amount) from modifiedlar2018
           where lei = ${lei.toUpperCase}
           and action_taken_type in (#${actionType})
           and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
           and (loan_purpose = 31 or loan_purpose = 32)
           and msa_md = ${msaMd}
           and tract_to_msamd = ${tractToMsaMd}
           and filing_year = ${filingYear}
           group by lei"""
        .as[LoanType]
        .headOption
        .map(_.getOrElse(LoanType("Refinancings (C)", 0, 0)))
    }
  }

  /**
    * Home Improvement Loans (D): Total Units = 1 through 4; Purpose of Loan = 2
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionD(lei: String,
                   msaMd: Int,
                   tractToMsaMd: String,
                   filingYear: Int,
                   actionType: String): Future[LoanType] = {
    db.run {
      sql"""select 'Home Improvement Loans (D)',count(*), sum(loan_amount) from modifiedlar2018
            where lei = ${lei.toUpperCase}
            and action_taken_type in (#${actionType})
            and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
            and loan_purpose = 2
            and msa_md = ${msaMd}
            and tract_to_msamd = ${tractToMsaMd}
            and filing_year = ${filingYear}
            group by lei"""
        .as[LoanType]
        .headOption
        .map(_.getOrElse(LoanType("Home Improvement Loans (D)", 0, 0)))
    }
  }

  /**
    * Loans on Dwellings For 5 or More Families (E): Total Units = 5+ Units
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionE(lei: String,
                   msaMd: Int,
                   tractToMsaMd: String,
                   filingYear: Int,
                   actionType: String): Future[LoanType] = {
    db.run {
      sql"""select 'Loans on Dwellings For 5 or More Families (E)',count(*), sum(loan_amount) from modifiedlar2018
            where lei = ${lei.toUpperCase}
            and action_taken_type in (#${actionType})
            and (total_units <> '1' and total_units <> '2' and total_units <> '3' and total_units <> '4')
            and msa_md = ${msaMd}
            and tract_to_msamd = ${tractToMsaMd}
            and filing_year = ${filingYear}
            group by lei"""
        .as[LoanType]
        .headOption
        .map(_.getOrElse(
          LoanType("Loans on Dwellings For 5 or More Families (E)", 0, 0)))
    }
  }

  /**
    * Nonoccupant Loans from Columns A, B, C ,& D (F): All loans from columns A through @ where Occupancy Type = 2, 3
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionF(lei: String,
                   msaMd: Int,
                   tractToMsaMd: String,
                   filingYear: Int,
                   actionType: String): Future[LoanType] = {
    db.run {
      sql"""select 'Nonoccupant Loans from Columns A, B, C ,& D (F)', count(*), sum(loan_amount) from modifiedlar2018
            where lei = ${lei.toUpperCase}
            and action_taken_type in (#${actionType})
            and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
            and (loan_purpose = 1 or loan_purpose = 2 or loan_purpose = 31 or loan_purpose = 32)
            and (loan_type = 1 or loan_type = 2 or loan_type = 3 or loan_type = 4)
            and (occupancy_type = 2 or occupancy_type = 3)
            and msa_md = ${msaMd}
            and tract_to_msamd = ${tractToMsaMd}
            and filing_year = ${filingYear}
            group by lei"""
        .as[LoanType]
        .headOption
        .map(_.getOrElse(
          LoanType("Nonoccupant Loans from Columns A, B, C ,& D (F)", 0, 0)))
    }
  }

  /**
    * Loans On Manufactured Home Dwellings From Columns A, B, C & D (G): Construction Method = 2
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionG(lei: String,
                   msaMd: Int,
                   tractToMsaMd: String,
                   filingYear: Int,
                   actionType: String): Future[LoanType] = {
    db.run {
      sql"""select 'Loans On Manufactured Home Dwellings From Columns A, B, C & D (G)',count(*), sum(loan_amount) from modifiedlar2018
            where lei = ${lei.toUpperCase}
            and action_taken_type in (#${actionType})
            and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
            and (loan_purpose = 1 or loan_purpose = 2 or loan_purpose = 31 or loan_purpose = 32)
            and (loan_type = 1 or loan_type = 2 or loan_type = 3 or loan_type = 4)
            and (construction_method = '2')
            and msa_md = ${msaMd}
            and tract_to_msamd = ${tractToMsaMd}
            and filing_year = ${filingYear}
            group by lei"""
        .as[LoanType]
        .headOption
        .map(
          _.getOrElse(LoanType(
            "Loans On Manufactured Home Dwellings From Columns A, B, C & D (G)",
            0,
            0)))
    }
  }

  /**
    * Fetch Msa
    * @param lei
    * @return the number of rows removed
    */
  def msaMdsByLei(lei: String, filingYear: Int): Future[Vector[MsaMd]] =
    db.run {
      sql"""SELECT DISTINCT msa_md, msa_md_name
                          FROM modifiedlar2018
                          WHERE UPPER(lei) = ${lei.toUpperCase} AND filing_year = ${filingYear} ORDER BY msa_md ASC"""
        .as[MsaMd]
    }

}
