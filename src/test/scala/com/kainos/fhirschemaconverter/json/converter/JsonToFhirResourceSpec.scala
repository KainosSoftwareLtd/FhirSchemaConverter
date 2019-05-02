package com.kainos.fhirschemaconverter.json.converter

import com.kainos.fhirschemaconverter.model._
import org.scalatest._
import play.api.libs.json.{JsValue, Json}

import scala.io.Source

class JsonToFhirResourceSpec extends FlatSpec with Matchers {

  "The FHIR Schema Converter" should
    "convert a sample schema file with one resource to an internal representation" in {

    val inputSchema = Json.parse(Source.fromResource("careConnectSchema/SingleResource.json")
      .getLines().mkString)

    val expected = Set(FhirResource(
      "CareConnect-AllergyIntolerance-1",
      "AllergyIntolerance",
      Set(FhirResourceProperty("id", StringType, List("meta")))))

    val result = JsonToFhirResource.convert(inputSchema)

    result shouldEqual expected
  }

  "The FHIR Schema Converter" should
    "convert a sample schema file with an array type resource to an internal representation" in {

    val inputSchema = Json.parse(Source.fromResource("careConnectSchema/ArrayResource.json")
      .getLines().mkString)

    val expected = Set(FhirResource(
      "CareConnect-AllergyIntolerance-1",
      "AllergyIntolerance",
      Set(
        FhirResourceProperty("coding", ArrayType, List("code")),
        FhirResourceProperty("code", StringType, List("code","coding","0"))
      )))

    val result = JsonToFhirResource.convert(inputSchema)

    result shouldEqual expected
  }
}
