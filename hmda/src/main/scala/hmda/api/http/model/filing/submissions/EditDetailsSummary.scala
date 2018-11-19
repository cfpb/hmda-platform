package hmda.api.http.model.filing.submissions

import hmda.model.edits.EditDetails

case class EditDetailsSummary(editName: String,
                              rows: Seq[EditDetails] = Nil,
                              path: String = "",
                              currentPage: Int = 0,
                              total: Int = 0)
    extends PaginatedResponse
