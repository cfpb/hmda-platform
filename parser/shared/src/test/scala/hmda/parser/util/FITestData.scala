package hmda.parser.util

object FITestData {

  val tsDAT =
    "100110132019201501171330 201320-1177984  551  SMALL BANK USA, NA            1850 TYSONS BLVD., SUITE 50             MCLEAN                   VA22102     BIGS USA, INC.                412 THIRD AVENUE                        NEW YORK                 NY10012     Bob Smith                     555-555-5555999-999-9999bob.smith@bank.com                                                "

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
