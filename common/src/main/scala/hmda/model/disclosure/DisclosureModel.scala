package hmda.model.disclosure

//case class LeiMsaMd(leiName: String, id: Int, name: String)

case class Msa (
                 id: Int,
                 name: String,
                 state: String,
                 stateName: String, //TODO: How do we fetch stateName?
               )
case class Disclosure (
                        lei: String,
                        institutionName: Option[String],
                        table: String,
                        `type`: String,
                        description: String,
                        year: Int,
                        reportDate: String,
                        msa: Msa,
                        tracts: Seq[Tract]
                      )

case class Disposition (
                         title: String,
                         values: Seq[LoanType]
                       )
case class Tract (
                   tract: String, //TODO: How do we fetch the complete tract name "state/county/tract" i.e. "NH/Hillsborough County/0131.00"
                   dispositions: Seq[Disposition]
                 )
case class LoanType (
                      dispositionName: String = "disposition name",
                      count: Int = 0,
                      value: Int = 0
                    )
//Size of a tract in modifiedlar table is 11 or NA. When it is 11, first two digits are state, next three are county, and last 6 are tract
case class TractDisclosure(stateCode: Int = 0, countyCode: Int = 0, tract: String = "NA")