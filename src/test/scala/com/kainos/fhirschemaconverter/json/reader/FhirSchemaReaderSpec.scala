package com.kainos.fhirschemaconverter.json.reader

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsArray, JsValue}

class FhirSchemaReaderSpec extends FlatSpec with Matchers {

  "The FHIR schema reader " should
    "return the Care Connect JSON schema with at least 80 resources" in {

    val result: JsValue = FhirSchemaReader.read()

    val resourceType = (result \ "resourceType" ).as[String]
    val totalResourceCount = (result \ "entry" ).as[JsArray].value.length

    resourceType shouldEqual "Bundle"
    totalResourceCount should be > 80
  }

}
