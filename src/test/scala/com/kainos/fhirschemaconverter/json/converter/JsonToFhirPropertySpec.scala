package com.kainos.fhirschemaconverter.json.converter

import com.kainos.fhirschemaconverter.model._
import org.scalatest._
import play.api.libs.json.{JsValue, Json}

class JsonToFhirPropertySpec extends FlatSpec with Matchers {

  "The FHIR Property Converter" should
    "convert a property with a string data type" in {

    val json: JsValue = Json.parse(
      """{
          "id": "AllergyIntolerance.meta.id",
          "path": "AllergyIntolerance.meta.id",
          "min": 0,
          "max": "1",
          "base": {
            "path": "Element.id",
            "min": 0,
            "max": "1"
          },
          "type": [
            {
              "code": "string"
            }
          ]
        }""")

    val expected = FhirResourceProperty("id", StringType, List("meta"))

    val result = JsonToFhirProperty.convert(json)

    result shouldEqual expected
  }

  "The FHIR Property Converter" should
    "convert a property with a choice data type" in {

    val json: JsValue = Json.parse(
      """
         {
          "id": "Observation.valueQuantity:valueQuantity.value",
          "path": "Observation.valueQuantity.value",
          "short": "Numerical value (with implicit precision)",
          "definition": "The value of the measured amount. The value includes an implicit precision in the presentation of the value.",
          "comment": "The implicit precision in the value should always be honored. Monetary values have their own rules for handling precision (refer to standard accounting text books).",
          "requirements": "Precision is handled implicitly in almost all cases of measurement.",
          "min": 1,
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
        }""")

    val expected = FhirResourceProperty("value", DoubleType, List("value", "Quantity"))

    val result = JsonToFhirProperty.convert(json)

    result shouldEqual expected
  }
}
