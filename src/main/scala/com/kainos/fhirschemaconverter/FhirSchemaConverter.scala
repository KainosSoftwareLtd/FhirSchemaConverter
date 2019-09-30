package com.kainos.fhirschemaconverter

import com.kainos.fhirschemaconverter.json.converter.JsonToFhirResource
import com.kainos.fhirschemaconverter.json.reader.FhirSchemaReader
import com.kainos.fhirschemaconverter.persistence.SqlWriter
import play.api.libs.json.JsValue

/**
  * Driver of application.
  * Takes FHIR schema in JSON format and generates DB views to make the data easy to query with SQL
  */
object FhirSchemaConverter extends App {

  //Hard coded boolean for swiching bewteen a file based schema and the FHIR StructureDefiniton schema.
  //Note: the fhir_base:latest docker image does not have any data in the (public) StructureDefiniton table.
  //so this will need an Evolve IC database to run.
  val fhirSchemaFromFile: Boolean = true;

  if (fhirSchemaFromFile) { 
    val jsonSchema: JsValue = FhirSchemaReader.read()
    val fhirResources = JsonToFhirResource.convert(jsonSchema)
    SqlWriter.createSqlViews(fhirResources)
  }
  else { 
    //Tables of interest to Evolve IC reporting project
    val tableList = List("Patient","Encounter","Observation", "MedicationOrder");

    for( table <- tableList){ 
      println("Processing table : " +table); 
      var fhirResources = JsonToFhirResource.convertFromDB(FhirSchemaReader.loadStructureDefinition(table))
      SqlWriter.createSqlViews(fhirResources)
    } 
  }

}
