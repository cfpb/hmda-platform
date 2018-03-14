package hmda.model.filing.lar

case class LarIdentifier(id: Int = 2,
                         LEI: Option[String] = None,
                         NMLSRIdentifier: String)
