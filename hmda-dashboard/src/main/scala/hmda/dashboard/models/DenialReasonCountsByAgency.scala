package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class DenialReasonCountsByAgency(
                                       agency: Int,
                                       dti_ratio: Int,
                                       employment_hist: Int,
                                       credit_hist: Int,
                                       collateral: Int,
                                       insufficient_cash: Int,
                                       unverified_info: Int,
                                       application_incomplete: Int,
                                       mortagage_ins_denied: Int,
                                       other: Int,
                                       exempt_count: Int
                                    )

object DenialReasonCountsByAgency {
  implicit val getResults: GetResult[DenialReasonCountsByAgency] = GetResult(r => DenialReasonCountsByAgency(r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<))

  implicit val codec: Codec[DenialReasonCountsByAgency] =
    Codec.forProduct11("agency","dti_ratio","employment_hist","credit_hist","collateral","insufficient_cash","unverified_info","application_incomplete","mortagage_ins_denied","other","exempt_count")(DenialReasonCountsByAgency.apply)(f => (f.agency,f.dti_ratio,f.employment_hist,f.credit_hist,f.collateral,f.insufficient_cash,f.unverified_info,f.application_incomplete,f.mortagage_ins_denied,f.other,f.exempt_count))
}
