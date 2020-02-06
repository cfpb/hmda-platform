package hmda.parser.derivedFields

import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}
import hmda.model.filing.lar.enums._
import hmda.model.filing.lar.LarGenerators._
import hmda.parser.derivedFields.SexCategorization._
import hmda.model.filing.lar.Sex

class SexCategorizationSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("Sex Categorization must identify sex categories") {
    
    forAll(larGen) { lar =>
        val notAvailableLar1 = lar.copy(applicant = lar.applicant.copy(sex = Sex(SexInformationNotProvided)), coApplicant = lar.coApplicant.copy(sex = Sex(SexNoCoApplicant)))
        val notAvailableLar2 = lar.copy(applicant = lar.applicant.copy(sex = Sex(SexNotApplicable)), coApplicant = lar.coApplicant.copy(sex = Sex(SexNoCoApplicant)))
        val notAvailableLar3 = lar.copy(applicant = lar.applicant.copy(sex = Sex(SexInformationNotProvided)), coApplicant = lar.coApplicant.copy(sex = Sex(Male)))
        val notAvailableLar4 = lar.copy(applicant = lar.applicant.copy(sex = Sex(SexNotApplicable)), coApplicant = lar.coApplicant.copy(sex = Sex(Male)))
        val notAvailableLar5 = lar.copy(applicant = lar.applicant.copy(sex = Sex(SexInformationNotProvided)), coApplicant = lar.coApplicant.copy(sex = Sex(Female)))
        val notAvailableLar6 = lar.copy(applicant = lar.applicant.copy(sex = Sex(SexNotApplicable)), coApplicant = lar.coApplicant.copy(sex = Sex(Female)))
        
        assignSexCategorization(notAvailableLar1) mustBe "Sex Not Available"
        assignSexCategorization(notAvailableLar2) mustBe "Sex Not Available"
        assignSexCategorization(notAvailableLar3) mustBe "Sex Not Available"
        assignSexCategorization(notAvailableLar4) mustBe "Sex Not Available"
        assignSexCategorization(notAvailableLar5) mustBe "Sex Not Available"
        assignSexCategorization(notAvailableLar6) mustBe "Sex Not Available"

        val maleLar1 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Male)), coApplicant = lar.coApplicant.copy(sex = Sex(Male)))
        val maleLar2 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Male)), coApplicant = lar.coApplicant.copy(sex = Sex(SexNoCoApplicant)))
        val maleLar3 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Male)), coApplicant = lar.coApplicant.copy(sex = Sex(SexInformationNotProvided)))
        val maleLar4 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Male)), coApplicant = lar.coApplicant.copy(sex = Sex(SexNotApplicable)))

        assignSexCategorization(maleLar1) mustBe Male.description
        assignSexCategorization(maleLar2) mustBe Male.description
        assignSexCategorization(maleLar3) mustBe Male.description
        assignSexCategorization(maleLar4) mustBe Male.description

        val femaleLar1 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Female)), coApplicant = lar.coApplicant.copy(sex = Sex(Female)))
        val femaleLar2 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Female)), coApplicant = lar.coApplicant.copy(sex = Sex(SexNoCoApplicant)))
        val femaleLar3 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Female)), coApplicant = lar.coApplicant.copy(sex = Sex(SexInformationNotProvided)))
        val femaleLar4 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Female)), coApplicant = lar.coApplicant.copy(sex = Sex(SexNotApplicable)))
        
        assignSexCategorization(femaleLar1) mustBe Female.description
        assignSexCategorization(femaleLar2) mustBe Female.description
        assignSexCategorization(femaleLar3) mustBe Female.description
        assignSexCategorization(femaleLar4) mustBe Female.description

        val jointLar1 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Female)), coApplicant = lar.coApplicant.copy(sex = Sex(Male)))
        val jointLar2 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Male)), coApplicant = lar.coApplicant.copy(sex = Sex(Female)))
        val jointLar3 = lar.copy(applicant = lar.applicant.copy(sex = Sex(MaleAndFemale)), coApplicant = lar.coApplicant.copy(sex = Sex(Male)))
        val jointLar4 = lar.copy(applicant = lar.applicant.copy(sex = Sex(MaleAndFemale)), coApplicant = lar.coApplicant.copy(sex = Sex(Female)))
        val jointLar5 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Female)), coApplicant = lar.coApplicant.copy(sex = Sex(MaleAndFemale)))
        val jointLar6 = lar.copy(applicant = lar.applicant.copy(sex = Sex(Male)), coApplicant = lar.coApplicant.copy(sex = Sex(MaleAndFemale)))

        assignSexCategorization(jointLar1) mustBe "Joint"
        assignSexCategorization(jointLar2) mustBe "Joint"
        assignSexCategorization(jointLar3) mustBe "Joint"
        assignSexCategorization(jointLar4) mustBe "Joint"
        assignSexCategorization(jointLar5) mustBe "Joint"
        assignSexCategorization(jointLar6) mustBe "Joint"
    }
  }
}