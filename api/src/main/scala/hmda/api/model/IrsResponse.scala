package hmda.api.model

import hmda.persistence.PaginatedResource
import hmda.query.model.filing.{ Msa, MsaSummary, MsaWithName }

case class Irs(msas: Seq[Msa]) {

  def paginatedResponse(page: Int, path: String): IrsResponse = {
    val total = msas.size
    val namedMsas = msas.map(_.addName)
    val p = PaginatedResource(total)(page)
    val pageOfMsas = namedMsas.slice(p.fromIndex, p.toIndex)
    val summary = MsaSummary.fromMsaCollection(msas)
    IrsResponse(pageOfMsas.toList, summary, path, page, total)
  }

}

case class IrsResponse(
  msas: List[MsaWithName],
  summary: MsaSummary,
  path: String,
  currentPage: Int,
  total: Int
) extends PaginatedResponse

/* When converted to JSON, IrsResponse has this format:

IrsResponse(
  msas: List[Msa],
  summary: MsaSummary,
  count: Int,
  total: Int,
  _links: PaginatedLinks)

This happens in MsaProtocol.scala */ 