package hmda.model.filing.lar

import hmda.model.filing.lar.enums._
import io.circe._
import io.circe.syntax._

case class Race(
                 race1: RaceEnum = new InvalidRaceCode,
                 race2: RaceEnum = new InvalidRaceCode,
                 race3: RaceEnum = new InvalidRaceCode,
                 race4: RaceEnum = new InvalidRaceCode,
                 race5: RaceEnum = new InvalidRaceCode,
                 otherNativeRace: String = "",
                 otherAsianRace: String = "",
                 otherPacificIslanderRace: String = "",
                 raceObserved: RaceObservedEnum = new InvalidRaceObservedCode
               )

object Race {
  implicit val raceEncoder: Encoder[Race] = (a: Race) =>
    Json.obj(
      ("race1", a.race1.asInstanceOf[LarEnum].asJson),
      ("race2", a.race2.asInstanceOf[LarEnum].asJson),
      ("race3", a.race3.asInstanceOf[LarEnum].asJson),
      ("race4", a.race4.asInstanceOf[LarEnum].asJson),
      ("race5", a.race5.asInstanceOf[LarEnum].asJson),
      ("otherNativeRace", Json.fromString(a.otherNativeRace)),
      ("otherAsianRace", Json.fromString(a.otherAsianRace)),
      ("otherPacificIslanderRace", Json.fromString(a.otherPacificIslanderRace)),
      ("raceObserved", a.raceObserved.asInstanceOf[LarEnum].asJson)
    )

  implicit val raceDecoder: Decoder[Race] = (c: HCursor) =>
    for {
      race1 <- c.downField("race1").as[Int]
      race2 <- c.downField("race2").as[Int]
      race3 <- c.downField("race3").as[Int]
      race4 <- c.downField("race4").as[Int]
      race5 <- c.downField("race5").as[Int]
      otherNativeRace <- c.downField("otherNativeRace").as[String]
      otherAsianRace <- c.downField("otherAsianRace").as[String]
      otherPacificIslanderRace <- c
        .downField("otherPacificIslanderRace")
        .as[String]
      raceObserved <- c.downField("raceObserved").as[Int]
    } yield
      Race(
        RaceEnum.valueOf(race1),
        RaceEnum.valueOf(race2),
        RaceEnum.valueOf(race3),
        RaceEnum.valueOf(race4),
        RaceEnum.valueOf(race5),
        otherNativeRace,
        otherAsianRace,
        otherPacificIslanderRace,
        RaceObservedEnum.valueOf(raceObserved)
      )
}