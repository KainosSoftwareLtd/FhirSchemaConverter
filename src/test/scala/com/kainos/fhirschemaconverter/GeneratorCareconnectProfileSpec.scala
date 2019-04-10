package com.kainos.fhirschemaconverter

import com.kainos.fhirschemaconverter.model._
import org.scalatest._
import play.api.libs.json.{JsValue, Json}

class GeneratorCareconnectProfileSpec extends FlatSpec with Matchers {

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

    val result: DataType = GeneratorCareConnectProfile.findDataType(json)

    result shouldEqual ArrayType
  }

  "The findDataType method" should "identify dates in a schema file from dateTime" in {
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

    val result: DataType = GeneratorCareConnectProfile.findDataType(json)

    result shouldEqual DateType
  }

  "The findDataType method" should "identify instances in a schema file as dates" in {
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

    val result: DataType = GeneratorCareConnectProfile.findDataType(json)

    result shouldEqual DateType
  }

  "The findDataType method" should "identify quantity in a schema file as double" in {
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

    val result: DataType = GeneratorCareConnectProfile.findDataType(json)

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

    val result: DataType = GeneratorCareConnectProfile.findDataType(json)

    result shouldEqual StringType
  }

  "The jsonPropertyToFhirResourceProperty method" should "convert a json property to a FHIR resource property" in {
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

    val result = GeneratorCareConnectProfile.jsonPropertyToFhirResourceProperty(json)

    result shouldEqual expected
  }
}
