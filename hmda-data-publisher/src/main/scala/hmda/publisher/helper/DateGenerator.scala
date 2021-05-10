package hmda.publisher.helper

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateGenerator {

  private val fullDate          = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  private val fullDateQuarterly = DateTimeFormatter.ofPattern("yyyy-MM-dd_")

  def currentDate = {
    val now = LocalDateTime.now().minusDays(1)
    fullDate.format(now)
  }

  def currentQuarterlyDate = {
    val now = LocalDateTime.now().minusDays(1)
    fullDateQuarterly.format(now)
  }
}
