package com.kainos.fhirschemaconverter

import com.kainos.fhirschemaconverter.model._
import play.api.libs.json.{JsArray, JsValue, Json}

import scala.io.Source

object GeneratorCareConnectProfile extends App {

  val jsonSchema: JsValue = Json.parse(Source.fromResource("care-connect-schema-definition-full.json")
    .getLines().mkString)
  val fhirResources = jsonToFhirResources(jsonSchema)
  SqlWriter.createSqlViews(fhirResources)

  private def jsonToFhirResources(jsonSchema: JsValue): Set[FhirResource] = {
    val resources: JsArray = (jsonSchema \ "entry").asOpt[JsArray].get

    resources.value
      //.filter(r=> (r \ "resource" \ "id").as[String].equals("CareConnect-Patient-1")) //to process single resource
      .map(resource => {
      val resourceId = (resource \ "resource" \ "id").as[String]
      val resourceType = (resource \ "resource" \ "type").as[String]
      println(s"Resource name: $resourceId")

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
      .split('.') //e.g. AllergyIntolerance.category
    var name = idList.last

    //TODO: Probably best to split this into a seperate view. Currently only capturing Quantity field but missing 3 others
    if (name.endsWith("[x]")) {
      name = name.replace("[x]", "")
      idList = idList.dropRight(1) :+ "value" :+ "Quantity" :+ name
    }
    val parents = if (hasParents(idList)) idList.drop(1).dropRight(1).toList else List()

    FhirResourceProperty(name, dataType, parents)
  }


  private def getFirstArrayElementIfPresent(arrayPaths: List[String],
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
