package com.hmda.reports.model

case class DataRaceEthnicity(msa_md: Long, msa_md_name: String, state: String, dispositionName: String, title: String, race: String, sex: String, ethnicity: String, loan_amount: Double, count: Long)

case class DispositionRaceEthnicity(name: String, count: Long, value: Double)
case class Gender(gender: String, dispositions: List[DispositionRaceEthnicity])
case class Ethnicity(ethnicityName: String, dispositions: List[DispositionRaceEthnicity], gender: List[Gender])
case class ReportByEthnicityThenGender(table: String, `type`: String, description: String, year: String, reportDate: String, msa: Msa, ethnicites: List[Ethnicity])
case class Race(race: String, dispositions: List[DispositionRaceEthnicity], genders: List[Gender])
case class ReportByRaceThenGender(table: String, `type`: String, description: String, year: String, reportDate: String, msa: Msa, races: List[Race])

case class Grouping(msa_md: Long, msa_md_name: String, state: String, dispositionName: String, title: String)