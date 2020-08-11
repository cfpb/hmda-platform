package hmda.institution.api.http.model

import hmda.institution.query.InstitutionNoteHistoryEntity

case class InstitutionNoteHistoryResponse(institutionNoteHistoryItems: Seq[InstitutionNoteHistoryEntity])
