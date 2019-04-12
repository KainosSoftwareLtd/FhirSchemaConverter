package com.kainos.fhirschemaconverter

import com.kainos.fhirschemaconverter.json.{FhirSchemaReader, JsonToFhirResourceConverter}
import com.kainos.fhirschemaconverter.persistence.SqlWriter
import play.api.libs.json.JsValue

object FhirSchemaConverter extends App {

  val jsonSchema: JsValue = FhirSchemaReader.read()
  val fhirResources = JsonToFhirResourceConverter.convert(jsonSchema)
  SqlWriter.createSqlViews(fhirResources)

}
