package hmda.model.util

object FITestData {

  val tsDAT =
    "100110132019201501171330 201320-1177984  551  SMALL BANK USA, NA            1850 TYSONS BLVD., SUITE 50             MCLEAN                   VA22102     BIGS USA, INC.                412 THIRD AVENUE                        NEW YORK                 NY10012     Bob Smith                     555-555-5555999-999-9999bob.smith@bank.com                                                "
  val tsDATNoParent =
    "100110132019201501171330 201320-1177984  551  SMALL BANK USA, NA            1850 TYSONS BLVD., SUITE 50             MCLEAN                   VA22102                                                                                                                Bob Smith                     555-555-5555999-999-9999bob.smith@bank.com                                                "

  val larsDAT = Seq(
    "201234567899ABCDEFGHIJKLMNOPQRSTUVWXY20130117432110000152013011906920060340100.01457432187654129000098701.0524B                                                                                                                                                                                                                                                                            x",
    "201234567899ABCDEFGHIJKLMNOPQRSTUVWXY20130117432110000152013011906920060340100.01457432187654129000098701.0524B                                                                                                                                                                                                                                                                            x",
    "201234567899ABCDEFGHIJKLMNOPQRSTUVWXY20130117432110000152013011906920060340100.01457432187654129000098701.0524B"
  )

  val fiDAT = Seq(
    "101234567899201301171330 201399-99999990000900MIKES SMALL BANK   XXXXXXXXXXX1234 Main St       XXXXXXXXXXXXXXXXXXXXXSacramento         XXXXXXCA99999-9999MIKES SMALL INC    XXXXXXXXXXX1234 Kearney St    XXXXXXXXXXXXXXXXXXXXXSan Francisco      XXXXXXCA99999-1234Mrs. Krabappel     XXXXXXXXXXX916-999-9999999-753-9999krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
    "201234567899ABCDEFGHIJKLMNOPQRSTUVWXY20130117432110000152013011906920060340100.01457432187654129000098701.0524B                                                                                                                                                                                                                                                                            x",
    "201234567899ABCDEFGHIJKLMNOPQRSTUVWXY20130117432110000152013011906920060340100.01457432187654129000098701.0524B                                                                                                                                                                                                                                                                            x",
    "201234567899ABCDEFGHIJKLMNOPQRSTUVWXY20130117432110000152013011906920060340100.01457432187654129000098701.0524B"
  )

  val fiCSV = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
    "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456790|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456791|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"

  val macroPasses = "1|8899000007|3|201802171330 |2017|99-6060622|0000129|Springfield Trust             |PO BOX 123                              |Springfield              |MA|01101     |Springfield National          |101 MAIN ST                             |Boston                   |MA|02108     |Bart Simpson                  |666-666-1111|555-555-1111|bsimpson@springfieldtrust.com\n" +
    "2|8899000007|3|20000001|20170422|1|1|1|1|00155|2|1|20170529|06920|18|131|9591.00|2|5|5| | | | |8| | | | |1|5|0054|0| | | |NA   |2|1\n" +
    "2|8899000007|3|20000003|20170110|1|1|1|1|00158|2|1|20170305|06920|18|181|9586.00|2|2|5| | | | |5| | | | |1|2|0163|9| | | |NA   |2|1\n" +
    "2|8899000007|3|20000011|20170622|1|1|3|1|00083|3|1|20170816|23844|18|111|1005.00|2|5|5| | | | |8| | | | |1|5|0057|9| | | |NA   |2|1\n" +
    "2|8899000007|3|20000016|20170724|1|1|1|1|00070|1|1|20171123|06920|18|131|9590.00|2|2|5| | | | |5| | | | |1|2|0024|9| | | |NA   |2|1\n" +
    "2|8899000007|3|20000020|20170709|1|2|1|1|00011|2|1|20170822|NA   |18|181|9582.00|2|2|5| | | | |5| | | | |1|2|0066|0| | | |04.17|2|1\n"

  val fiCSVParseError = "1|0123456789|9|timestamp|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
    "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|agencycode|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"

  val fiCSVEditErrors = " 1|8800009923|3|201801171330|2017|99-0123456|412|FIRST TEST BANK|1275 First Street NE|Washington|DC|20002|FIRST BANK PARENT INC.|1700 G ST NW|WASHINGTON|DC|20552|Brian Jones|666-666-6666|555-555-5555|brian.jones@bank.com\n" +
    "2|8800009923|11|8299422144|20170613|1|2|2|1|5|3|4|20170719|NA   |NA|NA |NA     |2|2|3| | | | |3| | | | |1|2|37|0| | | |NA   |2|1\n" +
    "2|8800009923|3|9471480396|20170919|1|1|2|1|40|3|4|20171018|NA   |45|067|9505.00|2|5|3| | | | |8| | | | |1|5|59|0| | | |NA   |2|1\n" +
    "33|8800009923|0|2185751599|20170328|1|1|2|1|25|3|3|20170425|NA   |45|067|9504.00|2|5|3| | | | |8| | | | |2|5|34|0| | | |NA   |2|1\n" +
    "2|8800009923|3|4977566612|20170920|1|1|1|1|46|3|3|20171022|NA   |45|067|9505.00|2|5|3| | | | |8| | | | |2|5|23|0| | | |NA   |2|0"

  val states = List(
    "AL",
    "AK",
    "AZ",
    "AR",
    "CA",
    "CO",
    "CT",
    "DE",
    "FL",
    "GA",
    "HI",
    "ID",
    "IL",
    "IN",
    "IA",
    "KS",
    "KY",
    "LA",
    "ME",
    "MD",
    "MA",
    "MI",
    "MN",
    "MS",
    "MO",
    "MT",
    "NE",
    "NV",
    "NH",
    "NJ",
    "NM",
    "NY",
    "NC",
    "ND",
    "OH",
    "OK",
    "OR",
    "PA",
    "RI",
    "SC",
    "SD",
    "TN",
    "TX",
    "UT",
    "VA",
    "WA",
    "WV",
    "WI",
    "WY",
    "AS",
    "DC",
    "FM",
    "GU",
    "MH",
    "MP",
    "PR",
    "VI"
  )

}
