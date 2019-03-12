package hmda.disclosure.model

case class Msa (
               id: String,
               name: String,
               state: String,
               stateName: String, //TODO: How do we fetch stateName?
               )
case class Tract (
                  tract: String, //TODO: How do we fetch the complete tract name "state/county/tract" i.e. "NH/Hillsborough County/0131.00"
                  dispositions: Seq[Disposition]
                 )
case class Disposition (
                        title: String,
                        values: Seq[LoanType]
                       )
case class LoanType (
                      dispositionName: String,
                      count: Int,
                      value: BigInt
                    )
case class Table1 (
                  lei: String,
                  institutionName: String,
                  table: String,
                  `type`: String,
                  description: String,
                  year: String,
                  reportDate: String,
                  tracts: Seq[Tract]
                  )
