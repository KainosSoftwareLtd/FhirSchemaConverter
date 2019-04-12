package com.kainos.fhirschemaconverter.json

import play.api.libs.json.{JsValue, Json}

import scala.io.Source

object FhirSchemaReader {

  def read(): JsValue = {
    Json.parse(Source.fromResource("care-connect-schema-definition-full.json")
      .getLines().mkString)
  }

}
