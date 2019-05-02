package com.kainos.fhirschemaconverter.json.converter

import com.kainos.fhirschemaconverter.model._
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.JsValue

/**
  * Converts json schema property to internal representation that's used for further processing
  * A resource contains one or more properties
  */
object JsonToFhirProperty extends StrictLogging {

  val fhirBaseTransformations: Boolean = true;

  def convert(property: JsValue): FhirResourceProperty = {
    val dataType = JsonToDataType.convert(property)

    val id = if (fhirBaseTransformations) {
      fhirBaseSpecificTransformations((property \ "id").as[String])
    } else{
      (property \ "id").as[String]
    }

    val idList: Array[String] = id.split('.')
    val name = idList.last

    val parents = if (hasParents(idList)) {
      idList.drop(1).dropRight(1).toList
    } else List()

    FhirResourceProperty(name, dataType, parents)
  }

  private def fhirBaseSpecificTransformations(input:String) = {
    input
      .replace("valueQuantity:valueQuantity","value.Quantity")
      .replace("valueCodeableConcept:valueCodeableConcept","value.CodeableConcept")
      .replace(":", ".")
  }

  private def hasParents(pathList: Array[String]) = {
    pathList.size > 2
  }

}
