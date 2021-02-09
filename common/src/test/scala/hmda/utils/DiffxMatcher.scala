package hmda.utils

import com.softwaremill.diffx.{ConsoleColorConfig, Diff, DiffResultDifferent}
import org.scalatest.matchers.{MatchResult, Matcher}

//copied from diffx-scalatest, cant be used directly because scalatest version mismatch
trait DiffxMatcher {
  def matchTo[A: Diff](right: A)(implicit c: ConsoleColorConfig): Matcher[A] = { left =>
    Diff[A].apply(left, right) match {
      case c: DiffResultDifferent =>
        val diff = c.show.split('\n').mkString(Console.RESET, s"${Console.RESET}\n${Console.RESET}", Console.RESET)
        MatchResult(matches = false, s"Matching error:\n$diff", "")
      case _ => MatchResult(matches = true, "", "")
    }
  }
}

object DiffxMatcher extends DiffxMatcher