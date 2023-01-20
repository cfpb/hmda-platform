package hmda.publisher.helper

import com.typesafe.config.{ Config, ConfigFactory }

object CronConfigLoader {
  private val dynamicQuartzScheduleConfig: Config = ConfigFactory.load().getConfig("akka.quartz.dynamic")
  val larPublicCron: String = dynamicQuartzScheduleConfig.getString("LarPublicSchedule.expression")
  val larPublicYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("LarPublicSchedule.years").split(",").map(s => s.toInt)
  val larCron: String = dynamicQuartzScheduleConfig.getString("LarSchedule.expression")
  val larQuarterlyCron: String = dynamicQuartzScheduleConfig.getString("LarQuarterlySchedule.expression")
  val loanLimitCron: String = dynamicQuartzScheduleConfig.getString("LarLoanLimitSchedule.expression")
  val larYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("LarSchedule.years").split(",").map(s => s.toInt)
  val larQuarterlyYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("LarQuarterlySchedule.years").split(",").map(s => s.toInt)
  val loanLimitYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("LarLoanLimitSchedule.years").split(",").map(s => s.toInt)
  val panelCron: String = dynamicQuartzScheduleConfig.getString("PanelSchedule.expression")
  val panelYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("PanelSchedule.years").split(",").map(s => s.toInt)
  val tsPublicCron: String = dynamicQuartzScheduleConfig.getString("TsPublicSchedule.expression")
  val tsPublicYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("TsPublicSchedule.years").split(",").map(s => s.toInt)
  val tsCron: String = dynamicQuartzScheduleConfig.getString("TsSchedule.expression")
  val tsYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("TsSchedule.years").split(",").map(s => s.toInt)
  val tsQuarterlyCron: String = dynamicQuartzScheduleConfig.getString("TsQuarterlySchedule.expression")
  val tsQuarterlyYears: Seq[Int] = dynamicQuartzScheduleConfig.getString("TsQuarterlySchedule.years").split(",").map(s => s.toInt)
}
