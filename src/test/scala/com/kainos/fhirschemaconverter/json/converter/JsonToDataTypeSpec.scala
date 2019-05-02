package com.kainos.fhirschemaconverter.json.converter

import com.kainos.fhirschemaconverter.model._
import org.scalatest._
import play.api.libs.json.{JsValue, Json}

class JsonToDataTypeSpec extends FlatSpec with Matchers {

  "The data type converter " should
    "work with 'string' as input" in {
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

    val result = JsonToDataType.convert(json)

    result shouldEqual StringType
  }


  "The data type converter " should
    "work with arrays as input" in {
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

    val result: DataType = JsonToDataType.convert(json)

    result shouldEqual ArrayType
  }

  "The data type converter " should
    "work with 'dateTime' as input" in {
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

    val result: DataType = JsonToDataType.convert(json)

    result shouldEqual DateType
  }

  "The data type converter " should
    "work with 'instant' as input" in {
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

    val result: DataType = JsonToDataType.convert(json)

    result shouldEqual DateType
  }

  "The data type converter " should
    "work with 'Quantity' as input" in {
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

    val result: DataType = JsonToDataType.convert(json)

    result shouldEqual DoubleType
  }

  "The data type converter " should
    "work with 'decimal' as input" in {
    val json: JsValue = Json.parse(
      """
         {
            "id": "Observation.valueQuantity:valueQuantity.value",
            "path": "Observation.valueQuantity.value",
            "min": 0,
            "max": "1",
            "base": {
                "path": "Quantity.value",
                "min": 0,
                "max": "1"
            },
            "type": [
                {
                    "code": "decimal"
                }
            ]
          }
  """)

    val result: DataType = JsonToDataType.convert(json)

    result shouldEqual DoubleType
  }

}
