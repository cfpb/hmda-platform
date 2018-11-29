package hmda.model.filing.submissions

import com.typesafe.config.ConfigFactory

case class PaginatedResource(totalRecords: Int, offset: Int = 0)(page: Int)
    extends WithPagination {
  def fromIndex: Int = calculateStartIndex(totalRecords, offset, page)
  def toIndex: Int = calculateEndIndex(totalRecords, offset, page)
}

trait WithPagination {
  private val config = ConfigFactory.load()
  def pageSize: Int = config.getInt("hmda.pageSize")

  def calculateStartIndex(total: Int, offset: Int, page: Int): Int = {
    val i = if (page == 1) 0 else pageSize * (page - 1) - offset
    Math.min(total, i)
  }

  def calculateEndIndex(total: Int, offset: Int, page: Int): Int = {
    Math.min(total, (page * pageSize) - offset)
  }
}
