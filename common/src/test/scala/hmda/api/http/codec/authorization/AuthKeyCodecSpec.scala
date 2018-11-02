package hmda.api.http.codec.authorization

import hmda.auth.AuthKeys
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalatest.{MustMatchers, WordSpec}

class AuthKeyCodecSpec extends WordSpec with MustMatchers {

  "AuthKeys" must {
    "deserialize from JSON" in {
      val str =
        """
          |{"keys":[{"kid":"AYUeqDHLF_GFsZYOSMXzhBT4zyQS--KiEmBFvMzJrBA","kty":"RSA","alg":"RS256","use":"sig","n":"sZjBy56hGpG31jlvHArCkwi2KMomyTgRT3t3xF_5q5mwyTaWR2fFNSNKD4I6cZkeOQ36fNE6ee5XMjLVFFVH335fLnsozNSL7IpXUaABl2Mwo6PVGhsVPQp0HfyOVkImUCtzR2Zd9Aaxz7NvIL0An49d_R6Z68_IfKvoeB7J-PSSY1QBoy-8PQ5OtZlrPaNgieR_pjNREO5TYZ-BiBW_4frlfObkHUb0tDDC_5C_XBCSMUM6p8deB9L2LJT4GgzrN2eZdgHPdxCeWWL04qN0F7oQ3mvXyJth7-ka__WXEvAbfUk8aUq_dBL9e3Acg44Ro4bpgXzph_uRyavqJMyTsw","e":"AQAB"}]}
        """.stripMargin

      val authKeys = decode[AuthKeys](str).getOrElse(AuthKeys())
      authKeys.keys.head.kid mustBe "AYUeqDHLF_GFsZYOSMXzhBT4zyQS--KiEmBFvMzJrBA"
    }
  }

}
