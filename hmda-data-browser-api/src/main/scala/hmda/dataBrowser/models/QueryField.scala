package hmda.dataBrowser.models

case class QueryField(name: String = "",
                      values: Seq[String] = List.empty,
                      dbName: String = "",
                      isAllSelected: Boolean = false)

case class LarQueryField(name: String = "",
                      value: String = "",
                      dbName: String = "",
                      isAllSelected: Boolean = false)

case class QueryFields(year: String = "2018", queryFields: List[QueryField] = List.empty)
