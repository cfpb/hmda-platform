package hmda.parser.filing.lar

import org.scalatest.{MustMatchers, WordSpec}

class LarParserExemptCodesSpec extends WordSpec with MustMatchers {

  val larExempt =
    "2|25KFHQ1AECMY7GZM9AQC|25KFHQ1AECMY7GZM9AQCF|20180325|4|4|2|2|1|460286|1|20180325|Exempt|Exempt|MI|Exempt|36043|36043011502|1|||||3258UM2W2PH4W86RKORFJVIOURDVVN|1|11||14||DARSPUYC7HKBG78Z1QPYOT|1|2|6|||||0GZGNKG9QC166F0M|2TMKTFC3NRPARAC0J8ICKYECVX|G3FFSA5M0W71VNFFDRVF8AHDBZO160KT2JOIIRIAYQHVXAQFSYWKV1DJU95ZH|22|2|21|23|27|55DIRQ85DSAQ50SRC6O4YS0HD9NKTEGI4M5NX550KQGMP5KB3P0D7IUYSSHE8ZPDITB6D9VPC5999||03YTFDILJBKE0E72INYWO0LF6H|3|2|4|1|3|1|29|27|NA|8|Exempt|3|2|1111|1111|1111||1111||1111|||||Exempt|Exempt|Exempt|Exempt|Exempt|Exempt|Exempt|Exempt|Exempt|Exempt|Exempt|1111|1111|1111|1111|Exempt|1111|1111|3|Exempt|1111|1111|Exempt|1111||||||1111||||||1111|1111|1111"

  "LAR Parser" must {
    "parse LAR with exempt codes" in {
      LarCsvParser(larExempt) match {
        case Right(_) => succeed
        case Left(_)  => fail
      }
    }
  }

}
