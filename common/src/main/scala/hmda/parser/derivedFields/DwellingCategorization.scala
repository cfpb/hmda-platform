package hmda.parser.derivedFields

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ ManufacturedHome, SiteBuilt }

package object DwellingCategorization {

  def assignDwellingCategorization(lar: LoanApplicationRegister): String =
    if (lar.property.totalUnits <= 4 && lar.loan.constructionMethod == SiteBuilt)
      "Single Family (1-4 Units):Site-Built"
    else if (lar.property.totalUnits > 4 && lar.loan.constructionMethod == SiteBuilt)
      "Multifamily:Site-Built"
    else if (lar.property.totalUnits <= 4 && lar.loan.constructionMethod == ManufacturedHome)
      "Single Family (1-4 Units):Manufactured"
    else if (lar.property.totalUnits > 4 && lar.loan.constructionMethod == ManufacturedHome)
      "Multifamily:Manufactured"
    else
      "N/A"
}
