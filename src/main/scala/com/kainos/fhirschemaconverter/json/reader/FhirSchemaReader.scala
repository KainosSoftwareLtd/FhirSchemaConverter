package com.kainos.fhirschemaconverter.json.reader

import play.api.libs.json.{JsValue, Json}

import java.sql.{Connection, DriverManager, ResultSet}
import com.typesafe.scalalogging.StrictLogging
import com.kainos.fhirschemaconverter.persistence.{SqlUtils,SqlReader}

import scala.io.Source

/**
  * Definition of FHIR Schema. 
  */
object FhirSchemaReader {

  // taken from file for simplicity 
  def read(): JsValue = {
    Json.parse(Source.fromResource("care-connect-schema-definition-full.json").getLines().mkString)

     // Json.parse(Source.fromResource("care-connect-patient-colla.json").getLines().mkString)
  }

  // read from FHIR DB
  def loadStructureDefinition(tableName: String): JsValue = {

     val sqlConnection: Connection = SqlUtils.getSqlConnection()

     var rs: ResultSet = SqlReader.fetchSchemaForTable (
                                    tableName,
                                    sqlConnection)

    Json.parse(rs.getString("resource"))

  }
}
