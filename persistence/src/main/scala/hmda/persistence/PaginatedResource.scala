package hmda.persistence

case class PaginatedResource(totalRecords: Int, page: Int, offset: Int)
    extends WithPagination {

  def fromIndex: Int = {
    Math.min(totalRecords, (page - 1) * pageSize)
  }
  def toIndex: Int = {
    Math.min(totalRecords, (page * pageSize) - offset)
  }

}

trait WithPagination {

  def pageSize: Int = 20

}
