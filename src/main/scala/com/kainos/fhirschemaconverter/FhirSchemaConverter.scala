package com.kainos.fhirschemaconverter

import com.kainos.fhirschemaconverter.json.converter.JsonToFhirResource
import com.kainos.fhirschemaconverter.json.reader.FhirSchemaReader
import com.kainos.fhirschemaconverter.persistence.SqlWriter
import play.api.libs.json.JsValue

/**
  * Takes FHIR schema in JSON format and generates DB views to make the data easy to query with SQL
  */
object FhirSchemaConverter extends App {

  val jsonSchema: JsValue = FhirSchemaReader.read()
  val fhirResources = JsonToFhirResource.convert(jsonSchema)
  SqlWriter.createSqlViews(fhirResources)
}
