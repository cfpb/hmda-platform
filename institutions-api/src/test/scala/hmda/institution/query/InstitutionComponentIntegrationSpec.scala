package hmda.institution.query

import com.typesafe.config.ConfigFactory
import hmda.institution.api.http.InstitutionQueryHttpApi
import hmda.query.DbConfiguration.dbConfig
import org.scalatest.WordSpec

class InstitutionComponentIntegrationSpec
  extends WordSpec
    with InstitutionEmailComponent {
  "be able to exercise institutions lifecycle management" in {
    val emailRepository               = new InstitutionEmailsRepository(dbConfig)

    institutionRepositories.values.foreach(repo => repo.createSchema())
    emailRepository.createSchema()

    institutionRepositories.values.foreach(repo => repo.getId(repo.table.baseTableRow))

    institutionRepositories.values.foreach(repo => repo.deleteById("RANDOMLEI"))

    institutionRepositories.values.foreach(repo => repo.dropSchema())
    emailRepository.dropSchema()
  }

  "be able to create a schema from within the Route" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    InstitutionQueryHttpApi.create(config = ConfigFactory.load("application-createschema.conf"))
  }
}