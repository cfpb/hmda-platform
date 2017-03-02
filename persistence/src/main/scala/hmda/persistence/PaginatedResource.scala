package hmda.persistence

case class PaginatedResource(totalRecords: Int, offset: Int)(page: Int)
    extends WithPagination {

  def fromIndex: Int = {
    val i = if (page == 1) 0 else pageSize * (page - 1) - offset
    Math.min(totalRecords, i)
  }
  def toIndex: Int = {
    Math.min(totalRecords, (page * pageSize) - offset)
  }

}

trait WithPagination {

  def pageSize: Int = 20

}
