package hmda.publication.lar

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._

package object SexCategorization {


  def assignSexCategorization(lar: LoanApplicationRegister): String = {
    val sex = lar.applicant.sex
    val coSex = lar.coApplicant.sex

   if (sex == SexInformationNotProvided ||sex == SexNotApplicable){
     "Sex not available"
   }else if(sex == Male && (coSex!=Female|| coSex != MaleAndFemale)){
     Male.toString
   }else if(sex == Female && (coSex!=Male|| coSex != MaleAndFemale)){
     Female.toString
   }else if(sex == Male && (coSex==Female|| coSex == MaleAndFemale)){
     "Joint"
   }else if(sex == Female && (coSex==Male|| coSex == MaleAndFemale)){
     "Joint"
   }else{
     "Not Determined"
   }
  }
}
