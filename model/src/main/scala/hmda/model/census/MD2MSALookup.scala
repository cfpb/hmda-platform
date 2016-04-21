package hmda.model.census

import java.io.File

object MD2MSALookup {
  val values = Seq(
    MD2MSA("14460", "14454", "1446014454", "Boston, MA", "Boston, MA Metro Division"),
    MD2MSA("14460", "15764", "1446015764", "Cambridge-Newton-Framingham, MA", "Cambridge-Newton-Framingham, MA Metro Division"),
    MD2MSA("14460", "40484", "1446040484", "Rockingham County-Strafford County, NH", "Rockingham County-Strafford County, NH Metro Division"),
    MD2MSA("16980", "16974", "1698016974", "Chicago-Naperville-Arlington Heights, IL", "Chicago-Naperville-Arlington Heights, IL Metro Division"),
    MD2MSA("16980", "20994", "1698020994", "Elgin, IL", "Elgin, IL Metro Division"),
    MD2MSA("16980", "23844", "1698023844", "Gary, IN", "Gary, IN Metro Division"),
    MD2MSA("16980", "29404", "1698029404", "Lake County-Kenosha County, IL-WI", "Lake County-Kenosha County, IL-WI Metro Division"),
    MD2MSA("19100", "19124", "1910019124", "Dallas-Plano-Irving, TX", "Dallas-Plano-Irving, TX Metro Division"),
    MD2MSA("19100", "23104", "1910023104", "Fort Worth-Arlington, TX", "Fort Worth-Arlington, TX Metro Division"),
    MD2MSA("19820", "19804", "1982019804", "Detroit-Dearborn-Livonia, MI", "Detroit-Dearborn-Livonia, MI Metro Division"),
    MD2MSA("19820", "47664", "1982047664", "Warren-Troy-Farmington Hills, MI", "Warren-Troy-Farmington Hills, MI Metro Division"),
    MD2MSA("31080", "11244", "3108011244", "Anaheim-Santa Ana-Irvine, CA", "Anaheim-Santa Ana-Irvine, CA Metro Division"),
    MD2MSA("31080", "31084", "3108031084", "Los Angeles-Long Beach-Glendale, CA", "Los Angeles-Long Beach-Glendale, CA Metro Division"),
    MD2MSA("33100", "22744", "3310022744", "Fort Lauderdale-Pompano Beach-Deerfield Beach, FL", "Fort Lauderdale-Pompano Beach-Deerfield Beach, FL Metro Division"),
    MD2MSA("33100", "33124", "3310033124", "Miami-Miami Beach-Kendall, FL", "Miami-Miami Beach-Kendall, FL Metro Division"),
    MD2MSA("33100", "48424", "3310048424", "West Palm Beach-Boca Raton-Delray Beach, FL", "West Palm Beach-Boca Raton-Delray Beach, FL Metro Division"),
    MD2MSA("35620", "20524", "3562020524", "Dutchess County-Putnam County, NY", "Dutchess County-Putnam County, NY Metro Division"),
    MD2MSA("35620", "35004", "3562035004", "Nassau County-Suffolk County, NY", "Nassau County-Suffolk County, NY Metro Division"),
    MD2MSA("35620", "35084", "3562035084", "Newark, NJ-PA", "Newark, NJ-PA Metro Division"),
    MD2MSA("35620", "35614", "3562035614", "New York-Jersey City-White Plains, NY-NJ", "New York-Jersey City-White Plains, NY-NJ Metro Division"),
    MD2MSA("37980", "15804", "3798015804", "Camden, NJ", "Camden, NJ Metro Division"),
    MD2MSA("37980", "33874", "3798033874", "Montgomery County-Bucks County-Chester County, PA", "Montgomery County-Bucks County-Chester County, PA Metro Division"),
    MD2MSA("37980", "37964", "3798037964", "Philadelphia, PA", "Philadelphia, PA Metro Division"),
    MD2MSA("37980", "48864", "3798048864", "Wilmington, DE-MD-NJ", "Wilmington, DE-MD-NJ Metro Division"),
    MD2MSA("41860", "36084", "4186036084", "Oakland-Hayward-Berkeley, CA", "Oakland-Hayward-Berkeley, CA Metro Division"),
    MD2MSA("41860", "41884", "4186041884", "San Francisco-Redwood City-South San Francisco, CA", "San Francisco-Redwood City-South San Francisco, CA Metro Division"),
    MD2MSA("41860", "42034", "4186042034", "San Rafael, CA	San Rafael, CA", "Metro Division"),
    MD2MSA("42660", "42644", "4266042644", "Seattle-Bellevue-Everett, WA", "Seattle-Bellevue-Everett, WA Metro Division"),
    MD2MSA("42660", "45104", "4266045104", "Tacoma-Lakewood, WA", "Tacoma-Lakewood, WA Metro Division"),
    MD2MSA("47900", "43524", "4790043524", "Silver Spring-Frederick-Rockville, MD", "Silver Spring-Frederick-Rockville, MD Metro Division"),
    MD2MSA("47900", "47894", "4790047894", "Washington-Arlington-Alexandria, DC-VA-MD-WV", "Washington-Arlington-Alexandria, DC-VA-MD-WV Metro Division")
  )

}

case class MD2MSA(msa: String, md: String, geoid: String, name: String, namelsad: String)

