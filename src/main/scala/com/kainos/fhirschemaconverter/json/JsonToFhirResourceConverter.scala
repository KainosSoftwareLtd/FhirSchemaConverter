package com.kainos.fhirschemaconverter.json

import com.kainos.fhirschemaconverter.model._
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{JsArray, JsValue}

object JsonToFhirResourceConverter extends LazyLogging {

  def convert(jsonSchema: JsValue): Set[FhirResource] = {
    val resources: JsArray = (jsonSchema \ "entry").asOpt[JsArray].get

    resources.value
      .map(resource => {
      val resourceId = (resource \ "resource" \ "id").as[String]
      val resourceType = (resource \ "resource" \ "type").as[String]
      logger.debug(s"Resource name: $resourceId")

      val properties: JsArray = (resource \ "resource" \ "snapshot" \ "element").asOpt[JsArray].get
      val fhirResourceProperties = properties.value.map(property => {
        jsonPropertyToFhirResourceProperty(property)
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

  def jsonPropertyToFhirResourceProperty(property: JsValue): FhirResourceProperty = {
    val dataType = findDataType(property)
    val id = (property \ "id").as[String]
    var idList: Array[String] = id
      .replace(":", ".")
      .split('.')
    var name = idList.last

    if (name.endsWith("[x]")) {
      name = name.replace("[x]", "")
      idList = idList.dropRight(1) :+ "value" :+ "Quantity" :+ name
    }
    val parents = if (hasParents(idList)) idList.drop(1).dropRight(1).toList else List()

    FhirResourceProperty(name, dataType, parents)
  }


  def getFirstArrayElementIfPresent(arrayPaths: List[String],
                                            parents: List[String]): List[String] = {
    if (parents.isEmpty) {
      parents
    }
    else {
      var parentListFlat = parents.mkString(".")
      arrayPaths.foreach(ap => parentListFlat = parentListFlat.replace(ap, ap + ".0"))
      parentListFlat.split('.').toList
    }

  }

  def findDataType(property: JsValue) = {

    val dataTypeFromJson = (property \ "type" \ 0 \ "code")
    if ((property \ "base" \ "max").as[String].equals("*")) {
      ArrayType
    }
    else if (dataTypeFromJson.isDefined) {
      dataTypeFromJson.as[String] match {
        case "dateTime" | "instant" => DateType
        case "Quantity" => DoubleType
        case _ => StringType
      }
    }
    else StringType
  }

  def hasParents(pathList: Array[String]) = {
    pathList.size > 2
  }

}
