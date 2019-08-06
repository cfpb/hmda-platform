package hmda.dataBrowser.models

case class QueryField(name: String = "",
                      values: Seq[String] = List.empty,
                      dbName: String = "",
                      isAllSelected: Boolean = false)
