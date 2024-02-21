package hmda.quarterly.data.api.route.counts.loans

import hmda.quarterly.data.api.route.lib.Verbiage

object Loans {
  private final val loansVerbiageConfig = Verbiage.config.getConfig("loan")
  final val CATEGORY = loansVerbiageConfig.getString("category")
  final val APP_VOLUME_TITLE = loansVerbiageConfig.getString("app_volume.title")
  final val APP_VOLUME_TITLE_HOME = loansVerbiageConfig.getString("app_volume.title") + " - Home Purchase"
  final val APP_VOLUME_TITLE_REFINANCE = loansVerbiageConfig.getString("app_volume.title") + " - Refinance"

  final val APP_VOLUME_SUBTITLE = loansVerbiageConfig.getString("app_volume.subtitle")

  final val ALL_APPS_VOLUME_TITLE = loansVerbiageConfig.getString("app_volume.all_apps_title")
  final val ALL_APPS_VOLUME_TITLE_HOME = loansVerbiageConfig.getString("app_volume.all_apps_title") + " - Home Purchase"
  final val ALL_APPS_VOLUME_TITLE_REFINANCE = loansVerbiageConfig.getString("app_volume.all_apps_title") + " - Refinance"

  final val ALL_APPS_VOLUME_SUBTITLE = loansVerbiageConfig.getString("app_volume.all_apps_subtitle")

  final val APP_LABEL = loansVerbiageConfig.getString("app_volume.label")

  final val LOAN_VOLUME_TITLE = loansVerbiageConfig.getString("loan_volume.title")
  final val LOAN_VOLUME_TITLE_HOME = loansVerbiageConfig.getString("loan_volume.title") + " - Home Purchase"
  final val LOAN_VOLUME_TITLE_REFINANCE = loansVerbiageConfig.getString("loan_volume.title") + " - Refinance"


  final val LOAN_VOLUME_SUBTITLE = loansVerbiageConfig.getString("loan_volume.subtitle")

  final val LOAN_LABEL = loansVerbiageConfig.getString("loan_volume.label")
  final val ALL_FILERS_LABEL = loansVerbiageConfig.getString("app_volume.all_filers_label")
  final val QUARTERLY_FILERS_LABEL = loansVerbiageConfig.getString("app_volume.quarterly_filers_label")
}