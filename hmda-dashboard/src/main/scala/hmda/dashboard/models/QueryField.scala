package hmda.dashboard.models

case class QueryField(name: String = "",
                      values: Seq[String] = List.empty,
                      dbName: String = "")

