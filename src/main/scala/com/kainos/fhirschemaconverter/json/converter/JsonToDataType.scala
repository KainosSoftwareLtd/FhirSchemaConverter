package com.kainos.fhirschemaconverter.json.converter

import com.kainos.fhirschemaconverter.model._
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.JsValue

/**
  * Determines data type of a property based on schema
  */
object JsonToDataType extends StrictLogging {

  def convert(property: JsValue) = {

/*
    val dataTypeFromJson = (property \ "type" \ 0 \ "code")
    //if ((property \ "base" \ "max").as[String].equals("*")) {
    //  ArrayType
    //}
    //else 
    if (dataTypeFromJson.isDefined) {
      dataTypeFromJson.as[String] match {
        case "dateTime" | "instant" => DateType
        case "decimal" => DoubleType
        case "Quantity" => ObjectType
        case _ => StringType
      }
    }
    //else StringType
    else ArrayType
*/
    val dataTypeFromJson = (property \ "type" \ 0 \ "code")
    val baseMaxFromJson = (property \ "base" \ "max")

    if (baseMaxFromJson.isDefined &&  baseMaxFromJson.as[String].equals("*")) {
      ArrayType
    }
    else if (dataTypeFromJson.isDefined) {
      dataTypeFromJson.as[String] match {
        case "dateTime" | "instant" => DateType
        case "decimal" => DoubleType
        case "Quantity" => ObjectType
        case _ => StringType
      }
    }
    else StringType
    
  }

}
