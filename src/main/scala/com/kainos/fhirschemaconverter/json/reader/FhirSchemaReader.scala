package com.kainos.fhirschemaconverter.json.reader

import play.api.libs.json.{JsValue, Json}

import scala.io.Source

/**
  * Definition of FHIR Schema. Currently taken from file for simplicity but could be read from DB or FHIR Rest API
  */
object FhirSchemaReader {

  def read(): JsValue = {
    Json.parse(Source.fromResource("care-connect-schema-definition-full.json")
    //Json.parse(Source.fromResource("patient.json")
      .getLines().mkString)
  }

}
