package hmda.publisher.helper

import com.typesafe.config.{ Config, ConfigFactory }
import org.quartz.CronExpression
import org.slf4j.LoggerFactory

import scala.concurrent.duration.TimeUnit
import scala.concurrent.duration._

object CronConfigLoader {
  private val log = LoggerFactory.getLogger(getClass)
  private val dynamicQuartzScheduleConfig: Config = ConfigFactory.load().getConfig("akka.quartz.dynamic")
  val larPublicCron: String = dynamicQuartzScheduleConfig.getString("LarPublicSchedule.expression")
  val larPublicYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("LarPublicSchedule.years").split(",").map(s => s.toInt)
  val larCron: String = dynamicQuartzScheduleConfig.getString("LarSchedule.expression")
  val larYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("LarSchedule.years").split(",").map(s => s.toInt)
  val larQuarterlyCron: String = dynamicQuartzScheduleConfig.getString("LarQuarterlySchedule.expression")
  val larQuarterlyYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("LarQuarterlySchedule.years").split(",").map(s => s.toInt)
  val loanLimitCron: String = dynamicQuartzScheduleConfig.getString("LarLoanLimitSchedule.expression")
  val loanLimitYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("LarLoanLimitSchedule.years").split(",").map(s => s.toInt)

  val combinedMlarCron: String = dynamicQuartzScheduleConfig.getString("CombinedMLarPublicSchedule.expression")
  val combinedMlarYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("CombinedMLarPublicSchedule.years").split(",").map(s => s.toInt)


  val panelCron: String = dynamicQuartzScheduleConfig.getString("PanelSchedule.expression")
  val panelYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("PanelSchedule.years").split(",").map(s => s.toInt)
  val tsPublicCron: String = dynamicQuartzScheduleConfig.getString("TsPublicSchedule.expression")
  val tsPublicYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("TsPublicSchedule.years").split(",").map(s => s.toInt)
  val tsCron: String = dynamicQuartzScheduleConfig.getString("TsSchedule.expression")
  val tsYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("TsSchedule.years").split(",").map(s => s.toInt)
  val tsQuarterlyCron: String = dynamicQuartzScheduleConfig.getString("TsQuarterlySchedule.expression")
  val tsQuarterlyYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("TsQuarterlySchedule.years").split(",").map(s => s.toInt)

  val specificLarCron: String = dynamicQuartzScheduleConfig.getString("SpecificLarSchedule.expression")
  val specificLarYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("SpecificLarSchedule.years").split(",").map(s => s.toInt)

  val specificPanelCron: String = dynamicQuartzScheduleConfig.getString("SpecificPanelSchedule.expression")
  val specificPanelYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("SpecificPanelSchedule.years").split(",").map(s => s.toInt)

  val specificTsCron: String = dynamicQuartzScheduleConfig.getString("SpecificTsSchedule.expression")
  val specificTsYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("SpecificTsSchedule.years").split(",").map(s => s.toInt)



  implicit class CronString(cron: String) {
    def applyOffset(offset: Int, unit: TimeUnit): String = {
      if (CronExpression.isValidExpression(cron)) {
        try {
          val (cronIdx, cadence) = unit match {
            case SECONDS => (0, 60)
            case MINUTES => (1, 60)
            case HOURS => (2, 24)
          }
          val parts = cron.split(" ").toSeq
          val baseTime = parts(cronIdx).toInt
          val newTime = (baseTime + offset) % cadence
          parts.updated(cronIdx, newTime).mkString(" ")
        } catch {
          case e: Throwable =>
            log.warn(s"Not able to apply offset to cron $cron", e)
            cron
        }
      } else {
        throw new IllegalArgumentException(s"Invalid Cron Expression: $cron")
      }
    }
  }
}
