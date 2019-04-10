package com.kainos.fhirschemaconverter

import java.sql.{Connection, DriverManager}

import com.kainos.fhirschemaconverter.model.{FhirResourceProperty, StringType}
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.collection.mutable.ListBuffer
import scala.io.Source

@deprecated
object GeneratorStandardFhir extends App {

  val jsonSchema: JsValue = Json.parse(Source.fromResource("standard-fhir-schema-full.json").getLines().mkString)
  val resourceName = "Patient"
  val fhirColumns = jsonPropertiesToFhirColumns(jsonSchema, resourceName, List[String](), new ListBuffer[FhirResourceProperty](), 0)
  createSqlView(resourceName, fhirColumns)

  private def jsonPropertiesToFhirColumns(jsonSchema: JsValue, resourceName: String,
                                          columnNamesOfParentResources: List[String],
                                          fhirColumns: ListBuffer[FhirResourceProperty], depth: Int): ListBuffer[FhirResourceProperty] = {
    println(s"DEPTH: $depth")

    if ((jsonSchema \ "definitions" \ resourceName \ "properties").isDefined) {
      val properties: JsValue = (jsonSchema \ "definitions" \ resourceName \ "properties").get
      val propertiesJsObject = properties.asOpt[JsObject].get
      propertiesJsObject.fieldSet
        .filter(!_._1.toLowerCase.contains("extension"))
        .foreach(row => {
          val (columnName, columnDefinition) = (row._1, row._2)
          println(s"Column Name: $columnName, Column Definition: $columnDefinition, columnNamesOfParentResources: ${columnNamesOfParentResources.mkString(",")}")

          if (depth <= 3) {
            fhirColumns.append(FhirResourceProperty(columnName, StringType, columnNamesOfParentResources))
            resolveChildSchemaReferences(jsonSchema, columnName, columnDefinition, columnNamesOfParentResources, fhirColumns, depth)
          }
        })
    }
    fhirColumns
  }

  private def resolveChildSchemaReferences(jsonSchema: JsValue, columnName: String, columnDefinitionJsValue: JsValue, previousColumnNames: List[String],
                                           fhirColumns: ListBuffer[FhirResourceProperty], depth: Int) = {
    if (isDefinitionResolveableLink(columnDefinitionJsValue.toString())) {
      // Only need to lookup non-simple datatypes. #/definitions/decimal doesn't match but #/definitions/CodeableConcept does. Simple types are always lower case
      val pattern = ".*#\\/definitions\\/([A-Z][A-Za-z]+).*".r
      columnDefinitionJsValue.toString() match {
        case pattern(childResourceName) => {
          var columnNamesOfParentResourcesWithAddition: List[String] = previousColumnNames :+ columnName
          jsonPropertiesToFhirColumns(jsonSchema, childResourceName, columnNamesOfParentResourcesWithAddition, fhirColumns, depth + 1)
        }
        case _ =>
      }
    }
  }

  private def isDefinitionResolveableLink(columnDefinition: String): Boolean = {
    columnDefinition.contains("#/definitions/") &&
      !columnDefinition.contains("definitions/Element")
  }

  private def isColumnEmpty(fhirColumn: FhirResourceProperty, columnSql: String, tableName: String, sql_connection: Connection): Boolean = {
    val statement = sql_connection.createStatement()
    val columnName = if (fhirColumn.columnNamesOfParentResources.isEmpty) fhirColumn.name else fhirColumn.columnNamesOfParentResources.mkString("_") + "_" + fhirColumn.name
    val sqlQuery = "select count(*) from (select " + columnSql +
      " from " + tableName + " a) sq where sq." + columnName + " is not null"

    println(s"Column check SQL Query: $sqlQuery")
    val resultSet = statement.executeQuery(sqlQuery)
    resultSet.next()
    val count = resultSet.getString("count")
    count.equals("0")
  }

  private def createSqlView(resourceName: String, fhirColumns: ListBuffer[FhirResourceProperty]): Unit = {

    Class.forName("org.postgresql.Driver")
    var sql_connection: Connection = DriverManager.getConnection("jdbc:postgresql://localhost/fhirbase", "postgres", "postgres")

    val tableName = resourceName.toLowerCase
    val viewName = s"${tableName}_view"
    val sqlCreateView = s"drop view if exists $viewName; create or replace view $viewName as select " +
      convertFhirColumnsToSqlColumns(fhirColumns, tableName, sql_connection).mkString(",") +
      s" from $tableName a"

    println(s"Full SQL to create view: $sqlCreateView")

    val prepare_statement = sql_connection.prepareStatement(sqlCreateView)
    prepare_statement.executeUpdate()
    prepare_statement.close()
  }

  private def convertFhirColumnsToSqlColumns(fhirColumns: ListBuffer[FhirResourceProperty], tableName: String, sql_connection: Connection): ListBuffer[String] = {
    var sqlColumnsList = new ListBuffer[String]()
    fhirColumns.foreach(fhirColumn => {
      val sqlColumn = fhirColumnToSqlColumn(fhirColumn)
      if (!isColumnEmpty(fhirColumn, sqlColumn, tableName, sql_connection)) sqlColumnsList.append(sqlColumn)
    })
    sqlColumnsList
  }

  private def fhirColumnToSqlColumn(fhirColumn: FhirResourceProperty): String = {
    if (fhirColumn.columnNamesOfParentResources.isEmpty) {
      "a.resource->>'" + fhirColumn.name + "' as " + fhirColumn.name
    }
    else {
      "a.resource->'" + fhirColumn.columnNamesOfParentResources.mkString("'->'") + "' ->> '" + fhirColumn.name + "' as " +
        (fhirColumn.columnNamesOfParentResources.mkString("_") + "_" + fhirColumn.name).toLowerCase()
    }

  }
}
