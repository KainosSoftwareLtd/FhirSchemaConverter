package com.kainos.fhirschemaconverter.json.converter

import com.kainos.fhirschemaconverter.model._
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.JsValue

/**
  * Determines data type of a property based on schema
  */
object JsonToDataType extends StrictLogging {

  def convert(property: JsValue) = {
    val dataTypeFromJson = (property \ "type" \ 0 \ "code")
    val maxFromJson = (property \ "max")

     //logger.debug("dataTypeFromJson: " + dataTypeFromJson.toString())
     //logger.debug("maxFromJson: " + maxFromJson.toString())

    if (maxFromJson.isDefined &&  maxFromJson.as[String].equals("*")) {
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
