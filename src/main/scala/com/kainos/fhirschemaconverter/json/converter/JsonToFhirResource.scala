package com.kainos.fhirschemaconverter.json.converter

import com.kainos.fhirschemaconverter.model._
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.{JsArray, JsValue, JsObject, Json}

/**
  * Converts json schema resource to internal representation that's used for further processing
  */
object JsonToFhirResource extends StrictLogging {

  def convert(jsonSchema: JsValue): Set[FhirResource] = {

    logger.debug("Entry into JsonToFhirResource.convert")

    val resources: JsArray = (jsonSchema \ "entry").asOpt[JsArray].get

    resources.value
      .map(resource => {
      val resourceId = (resource \ "resource" \ "id").as[String]
      val resourceType = (resource \ "resource" \ "type").as[String]
      logger.debug(s"Resource name: $resourceId")

      val properties: JsArray = (resource \ "resource" \ "snapshot" \ "element").asOpt[JsArray].get

      val fhirResourceProperties = properties.value.map(property => {

       // logger.debug(property.toString())
        
        JsonToFhirProperty.convert(property)
      }).toSet

      val arrayProperties = fhirResourceProperties.filter(r => r.dataType.equals(ArrayType))

      val pathsOfArrayProperties: List[String] = arrayProperties.map(v =>
        (v.columnNamesOfParentResources :+ v.name).mkString(".")).toList.sorted.reverse

      val fhirResourcePropertiesWithArraySelection = fhirResourceProperties.map(r =>
        FhirResourceProperty(r.name, r.dataType, getFirstArrayElementIfPresent(
          pathsOfArrayProperties, r.columnNamesOfParentResources)))

      FhirResource(resourceId, resourceType, fhirResourcePropertiesWithArraySelection)
    }).toSet
  }

  def convertFromDB (jsonSchema: JsValue): Set[FhirResource] = {

    logger.debug("Entry into JsonToFhirResource.convertFromDB")

    //logger.debug(jsonSchema.toString())

    val resourceId = (jsonSchema \ "id").as[String]
    val resourceType = (jsonSchema \ "type").as[String]

      logger.debug(s"Resource name: $resourceId")
      logger.debug(s"Resource type: $resourceType")

      val properties: JsArray = (jsonSchema \ "snapshot" \ "element").asOpt[JsArray].get

      val fhirResourceProperties = properties.value.map(property => {
        JsonToFhirProperty.convert(property)
      }).toSet

      val arrayProperties = fhirResourceProperties.filter(r => r.dataType.equals(ArrayType))

      val pathsOfArrayProperties: List[String] = arrayProperties.map(v =>
        (v.columnNamesOfParentResources :+ v.name).mkString(".")).toList.sorted.reverse

      val fhirResourcePropertiesWithArraySelection = fhirResourceProperties.map(r =>
        FhirResourceProperty(r.name, r.dataType, getFirstArrayElementIfPresent(
          pathsOfArrayProperties, r.columnNamesOfParentResources)))

      Set(FhirResource(resourceId, resourceType, fhirResourcePropertiesWithArraySelection))
  }

  /**
    * Adds 0 to the list of parents to get the first element
    *
    * e.g.
    * Given: arrayPaths = ["a.b"] , parents = ["a","b","c"]
    * Result: ["a"."b"."0"."c"]
    */
  private def getFirstArrayElementIfPresent(arrayPaths: List[String],
                                            parents: List[String]): List[String] = {
    if (parents.isEmpty) {
      parents
    }
    else {
      var parentListFlat = parents.mkString(".")
      arrayPaths.foreach(
        ap => parentListFlat = parentListFlat.replace(ap, ap + ".0"))
      parentListFlat.split('.').toList
    }

  }
}
