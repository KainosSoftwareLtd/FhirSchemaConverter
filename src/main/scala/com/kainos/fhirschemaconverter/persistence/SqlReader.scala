package com.kainos.fhirschemaconverter.persistence

import java.sql.{Connection, DriverManager, ResultSet}

import com.kainos.fhirschemaconverter.model._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

/**
  * Creates views in database based on our internal collection of FHIR resources
  */
object SqlReader extends StrictLogging {

   def fetchSchemaForTable (tableName: String,
                            sql_connection: Connection): ResultSet = {


    //Is a tenant schema set in application.conf
    val conf = ConfigFactory.load
    var schema = conf.getString("output.db.schema")

    if (schema.length() > 0) { 
        schema = schema.concat(".")
     }

    val getStructureDefinition = s"select (resource - 'text')::text as resource from $schema" + 
                                 s"structuredefinition " + 
                                 s"where id = '$tableName' "

    logger.debug("SQL to getStructureDefinition: " + getStructureDefinition)

    val statement = sql_connection.createStatement()
    val resultSet: ResultSet = statement.executeQuery(getStructureDefinition)
    resultSet.next()
    resultSet
  }
}
