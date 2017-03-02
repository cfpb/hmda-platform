package hmda.api.model

import hmda.persistence.WithPagination

trait PaginatedResponse extends WithPagination {
  def path: String
  def currentPage: Int
  def total: Int

  def links = PaginationLinks(
    configurablePath,
    pageQuery(currentPage),
    pageQuery(1),
    pageQuery(prevPage),
    pageQuery(nextPage),
    pageQuery(lastPage)
  )
  def count: Int = {
    if (validPage) {
      if (currentPage == lastPage) lastPageCount
      else pageSize
    } else 0
  }

  ///// Helper Methods /////

  private def validPage: Boolean = currentPage >= 1 && currentPage <= lastPage

  private def configurablePath: String = s"$path{rel}"

  private def lastPage: Int = {
    if (total % pageSize == 0) fullPages
    else fullPages + 1
  }
  private def prevPage: Int = {
    if (currentPage < 2) 1
    else currentPage - 1
  }
  private def nextPage: Int = {
    if (currentPage >= lastPage - 1) lastPage
    else currentPage + 1
  }
  private def fullPages: Int = total / pageSize

  private def lastPageCount: Int = {
    val remainder = total % pageSize
    if (remainder == 0) pageSize
    else remainder
  }

  private def pageQuery(n: Int): String = s"?page=$n"
}

case class PaginationLinks(
  href: String,
  self: String,
  first: String,
  prev: String,
  next: String,
  last: String
)

object PaginatedResponse {
  def staticPath(configurablePath: String): String = {
    val extractPath = """(.+)\{rel\}""".r

    configurablePath match {
      case extractPath(path) => path
    }
  }

  def currentPage(paginationQueryString: String): Int = {
    val extractPage = """.*(\d+)""".r

    paginationQueryString match {
      case extractPage(page) => page.toInt
    }
  }
}
