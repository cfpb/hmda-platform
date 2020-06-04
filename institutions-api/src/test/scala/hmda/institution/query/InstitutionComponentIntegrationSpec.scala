package hmda.institution.query

import com.typesafe.config.ConfigFactory
import hmda.institution.api.http.InstitutionQueryHttpApi
import hmda.query.DbConfiguration.dbConfig
import org.scalatest.WordSpec

class InstitutionComponentIntegrationSpec
  extends WordSpec
    with InstitutionComponent2018
    with InstitutionComponent2019
    with InstitutionComponent2020
    with InstitutionEmailComponent {
  "be able to exercise institutions lifecycle management" in {
    val institutionRepository2018     = new InstitutionRepository2018(dbConfig, "institutions2018")
    val institutionRepository2018Beta = new InstitutionRepository2018Beta(dbConfig, "institutions2018_beta")
    val institutionRepository2019     = new InstitutionRepository2019(dbConfig, "institutions2019")
    val institutionRepository2019Beta = new InstitutionRepository2019Beta(dbConfig, "institutions2019_beta")
    val institutionRepository2020     = new InstitutionRepository2020(dbConfig, "institutions2020")
    val institutionRepository2020Beta = new InstitutionRepository2020Beta(dbConfig, "institutions2020_beta")
    val emailRepository               = new InstitutionEmailsRepository(dbConfig)

    institutionRepository2018.createSchema()
    institutionRepository2018Beta.createSchema()
    institutionRepository2019.createSchema()
    institutionRepository2019Beta.createSchema()
    institutionRepository2020.createSchema()
    institutionRepository2020Beta.createSchema()
    emailRepository.createSchema()

    institutionRepository2018.getId(institutionRepository2018.table.baseTableRow)
    institutionRepository2019.getId(institutionRepository2019.table.baseTableRow)
    institutionRepository2020.getId(institutionRepository2020.table.baseTableRow)

    institutionRepository2018Beta.getId(institutionRepository2018Beta.table.baseTableRow)
    institutionRepository2019Beta.getId(institutionRepository2019Beta.table.baseTableRow)
    institutionRepository2020Beta.getId(institutionRepository2020Beta.table.baseTableRow)

    institutionRepository2018.deleteById("RANDOMLEI")
    institutionRepository2019.deleteById("RANDOMLEI")
    institutionRepository2020.deleteById("RANDOMLEI")

    institutionRepository2018Beta.deleteById("RANDOMLEI")
    institutionRepository2019Beta.deleteById("RANDOMLEI")
    institutionRepository2020Beta.deleteById("RANDOMLEI")

    institutionRepository2018.dropSchema()
    institutionRepository2018Beta.dropSchema()
    institutionRepository2019.dropSchema()
    institutionRepository2019Beta.dropSchema()
    institutionRepository2020.dropSchema()
    institutionRepository2020Beta.dropSchema()
    emailRepository.dropSchema()
  }

  "be able to create a schema from within the Route" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    InstitutionQueryHttpApi.create(config = ConfigFactory.load("application-createschema.conf"))
  }
}