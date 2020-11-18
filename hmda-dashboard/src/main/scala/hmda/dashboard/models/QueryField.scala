package hmda.dashboard.models

case class QueryField(name: String,
                      value: Seq[String])

case class QueryFields(name: String, value: String)
