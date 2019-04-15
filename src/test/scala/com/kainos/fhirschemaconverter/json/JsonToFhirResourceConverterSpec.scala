package com.kainos.fhirschemaconverter.json

import com.kainos.fhirschemaconverter.model._
import org.scalatest._
import play.api.libs.json.{JsValue, Json}

import scala.io.Source

class JsonToFhirResourceConverterSpec extends FlatSpec with Matchers {

  "The FHIR Schema Converter" should "convert a sample schema file with one resource to our internal representation" in {

    val inputSchema = Json.parse(Source.fromResource("CareConnectSchemaSubsetSingleResource.json")
      .getLines().mkString)

    val expected = Set(FhirResource(
      "CareConnect-AllergyIntolerance-1",
      "AllergyIntolerance",
      Set(FhirResourceProperty("id", StringType, List("meta")))))

    val result = JsonToFhirResourceConverter.convert(inputSchema)

    result shouldEqual expected
  }

  "The single json property converter" should
    "convert a json property to a FHIR resource property" in {
    val json: JsValue = Json.parse(
      """
         {
            "id": "Patient.name.family",
            "path": "Patient.name.family",
            "min": 0,
            "max": "1",
            "base": {
                "path": "HumanName.family",
                "min": 0,
                "max": "1"
            },
            "type": [
                {
                    "code": "string"
                }
            ]
          }
  """)

    val expected = FhirResourceProperty("family",StringType,List("name"))

    val result = JsonToFhirResourceConverter.jsonPropertyToFhirResourceProperty(json)

    result shouldEqual expected
  }

  "The findDataType method" should "identify arrays in a schema file" in {
    val json: JsValue = Json.parse(
      """
         {
          "id": "AllergyIntolerance.category",
          "path": "AllergyIntolerance.category",
          "min": 0,
          "max": "*",
          "base": {
              "path": "AllergyIntolerance.category",
              "min": 0,
              "max": "*"
          }
           }
  """)

    val result: DataType = JsonToFhirResourceConverter.findDataType(json)

    result shouldEqual ArrayType
  }

  "The findDataType method" should "identify dates in a schema file from 'dateTime' code" in {
    val json: JsValue = Json.parse(
      """
         {
          "id": "AllergyIntolerance.assertedDate",
          "path": "AllergyIntolerance.assertedDate",
          "min": 1,
          "max": "1",
          "base": {
              "path": "AllergyIntolerance.assertedDate",
              "min": 0,
              "max": "1"
          },
          "type": [
              {
                  "code": "dateTime"
              }
          ]
         }
  """)

    val result: DataType = JsonToFhirResourceConverter.findDataType(json)

    result shouldEqual DateType
  }

  "The findDataType method" should "identify dates in a schema file from 'instant' code" in {
    val json: JsValue = Json.parse(
      """
          {
            "id": "Procedure.meta.lastUpdated",
            "path": "Procedure.meta.lastUpdated",
            "min": 0,
            "max": "1",
            "base": {
                "path": "Resource.meta.lastUpdated",
                "min": 0,
                "max": "1"
            },
            "type": [
                {
                    "code": "instant"
                }
            ]
          }
  """)

    val result: DataType = JsonToFhirResourceConverter.findDataType(json)

    result shouldEqual DateType
  }

  "The findDataType method" should "identify 'Quantity' in a schema file as double" in {
    val json: JsValue = Json.parse(
      """
         {
            "id": "Observation.referenceRange.low",
            "path": "Observation.referenceRange.low",
            "min": 0,
            "max": "1",
            "base": {
                "path": "Observation.referenceRange.low",
                "min": 0,
                "max": "1"
            },
            "type": [
                {
                    "code": "Quantity",
                    "profile": "http://hl7.org/fhir/StructureDefinition/SimpleQuantity"
                }
            ]
          }
  """)

    val result: DataType = JsonToFhirResourceConverter.findDataType(json)

    result shouldEqual DoubleType
  }

  "The findDataType method" should "identify string in a schema file as string" in {
    val json: JsValue = Json.parse(
      """
         {
            "id": "Patient.name.family",
            "path": "Patient.name.family",
            "min": 0,
            "max": "1",
            "base": {
                "path": "HumanName.family",
                "min": 0,
                "max": "1"
            },
            "type": [
                {
                    "code": "string"
                }
            ]
          }
  """)

    val result: DataType = JsonToFhirResourceConverter.findDataType(json)

    result shouldEqual StringType
  }

}
