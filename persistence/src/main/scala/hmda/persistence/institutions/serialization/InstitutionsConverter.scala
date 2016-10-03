package hmda.persistence.institutions.serialization

import hmda.model.fi._
import hmda.model.institution.Agency._
import hmda.model.institution.ExternalIdType._
import hmda.model.institution.InstitutionStatus.{ Active, Inactive }
import hmda.model.institution.InstitutionType._
import hmda.model.institution._
import hmda.model.institutions._

import scala.language.implicitConversions

object InstitutionsConverter {

  implicit def messageToInstitution(m: Option[InstitutionMessage]): Institution = {
    m.map { i =>
      val id = i.id
      val name = i.name
      val externalIds = i.externalId.map { x =>
        val id = x.id
        val idType = x.idType.value match {
          case 0 => ExternalIdType.FdicCertNo
          case 1 => ExternalIdType.FederalTaxId
          case 2 => ExternalIdType.NcuaCharterId
          case 3 => ExternalIdType.OccCharterId
          case 4 => ExternalIdType.RssdId
        }
        ExternalId(id, idType)
      }

      val agency = i.agency.value match {
        case 0 => Agency.CFPB
        case 1 => Agency.FDIC
        case 2 => Agency.FRS
        case 3 => Agency.HUD
        case 4 => Agency.NCUA
        case 5 => Agency.OCC
      }
      val institutionType = i.institutionType.value match {
        case 0 => InstitutionType.Bank
        case 1 => InstitutionType.CreditUnion
        case 2 => InstitutionType.SavingsAndLoan
        case 3 => InstitutionType.IndependentMortgageCompany
        case 4 => InstitutionType.MBS
        case 5 => InstitutionType.Affiliate
        case 6 => InstitutionType.NonDepositInstType
        case 7 => InstitutionType.NoDepositTypeInstType
      }
      val hasParent = i.hasParent
      val cra = i.cra
      val status = i.status.value match {
        case 0 => InstitutionStatus.Active
        case 1 => InstitutionStatus.Inactive
      }
      Institution(
        id,
        name,
        externalIds.toSet[ExternalId],
        agency,
        institutionType,
        hasParent,
        cra,
        status
      )
    }.getOrElse(Institution(
      "",
      "",
      Set.empty[ExternalId],
      Agency.CFPB,
      InstitutionType.Bank,
      false,
      false,
      InstitutionStatus.Inactive
    ))
  }

  implicit def institutionToMessage(institution: Institution): Option[InstitutionMessage] = {
    val id = institution.id
    val name = institution.name
    val externalIds = institution.externalIds.map { x =>
      val id = x.id
      val idType = x.idType match {
        case FdicCertNo => ExternalIdTypeMessage.FDIC_CERT_NO
        case FederalTaxId => ExternalIdTypeMessage.FEDERAL_TAX_ID
        case NcuaCharterId => ExternalIdTypeMessage.NCUA_CHARTER_ID
        case OccCharterId => ExternalIdTypeMessage.OCC_CHARTER_ID
        case RssdId => ExternalIdTypeMessage.RSS_ID
      }
      ExternalIdMessage(id, idType)
    }
    val agency = institution.agency match {
      case CFPB => AgencyMessage.CFPB
      case FDIC => AgencyMessage.FDIC
      case FRS => AgencyMessage.FRS
      case HUD => AgencyMessage.HUD
      case NCUA => AgencyMessage.NCUA
      case OCC => AgencyMessage.OCC
    }
    val institutionType = institution.institutionType match {
      case Bank => InstitutionTypeMessage.BANK
      case CreditUnion => InstitutionTypeMessage.CREDIT_UNION
      case SavingsAndLoan => InstitutionTypeMessage.SAVINGS_AND_LOAN
      case IndependentMortgageCompany => InstitutionTypeMessage.INDEPENDENT_MORTGAGE_COMPANY
      case Affiliate => InstitutionTypeMessage.AFFILIATE
      case MBS => InstitutionTypeMessage.MBS
      case NonDepositInstType => InstitutionTypeMessage.NON_DEPOSIT_INST_TYPE
      case NoDepositTypeInstType => InstitutionTypeMessage.NO_DEPOSIT_TYPE_INST_TYPE
    }
    val hasParent = institution.hasParent
    val cra = institution.cra
    val status = institution.status match {
      case Active => InstitutionStatusMessage.ACTIVE
      case Inactive => InstitutionStatusMessage.INACTIVE
    }
    Some(InstitutionMessage(
      id,
      name,
      externalIds.toSeq,
      agency,
      institutionType,
      hasParent,
      cra,
      status
    ))
  }

  implicit def messageToFilingCreated(m: Option[FilingMessage]): Filing = {
    m.map { f =>
      val id = f.institutionId
      val period = f.period
      val filingStatus = f.status.value match {
        case 0 => NotStarted
        case 1 => InProgress
        case 2 => Completed
        case 3 => Cancelled
      }
      Filing(period, id, filingStatus)
    }.getOrElse(Filing())
  }

  implicit def filingToMessage(filing: Filing): Option[FilingMessage] = {
    val period = filing.period
    val institutionId = filing.institutionId
    val status = filing.status match {
      case NotStarted => FilingStatusMessage.NOT_STARTED
      case InProgress => FilingStatusMessage.IN_PROGRESS
      case Completed => FilingStatusMessage.COMPLETED
      case Cancelled => FilingStatusMessage.CANCELLED
    }
    val message = FilingMessage(period, institutionId, status)
    Some(message)
  }

}
