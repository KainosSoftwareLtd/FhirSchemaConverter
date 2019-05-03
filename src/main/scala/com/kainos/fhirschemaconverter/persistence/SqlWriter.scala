package com.kainos.fhirschemaconverter.persistence

import java.sql.{Connection, DriverManager, ResultSet}

import com.kainos.fhirschemaconverter.FhirPropertyToSqlColumn
import com.kainos.fhirschemaconverter.model._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

/**
  * Creates views in database based on our internal collection of FHIR resources
  */
object SqlWriter extends StrictLogging {

  def createSqlViews(fhirResources: Set[FhirResource]): Unit = {

    //loaded from resources/application.conf if no overrides set in environment
    val conf = ConfigFactory.load
    val hostname = conf.getString("output.db.host")
    val port = conf.getString("output.db.port")
    val database = conf.getString("output.db.database")
    val username = conf.getString("output.db.username")
    val password = conf.getString("output.db.password")

    Class.forName("org.postgresql.Driver")
    val sqlConnection: Connection = DriverManager.getConnection(
      s"jdbc:postgresql://$hostname:$port/$database", username, password)

    fhirResources.filter(
      r => SqlUtils.tableExists(r.tableName.toLowerCase, sqlConnection))
      .foreach(fhirResource => {
        val tableName = fhirResource.tableName.toLowerCase()
        val viewName = s"${fhirResource.id.replace("-", "_")}_view"
        val sqlColumns = convertFhirColumnsToSqlColumns(fhirResource.properties, tableName, sqlConnection)
        val sqlCreateView = s"drop view if exists $viewName; create or replace view $viewName as select " +
          sqlColumns.mkString(",") +
          s" from $tableName a"

        logger.debug(s"Full SQL to create view: $sqlCreateView")

        if (sqlColumns.nonEmpty && SqlUtils.tableExists(tableName, sqlConnection)) {
          val prepare_statement = sqlConnection.prepareStatement(sqlCreateView)
          prepare_statement.executeUpdate()
          prepare_statement.close()
        }
      }
      )
  }

  private def convertFhirColumnsToSqlColumns(fhirColumns: Set[FhirResourceProperty],
                                             tableName: String,
                                             sql_connection: Connection): Set[String] = {

    val columnCountResults: ResultSet = checkColumnCounts(fhirColumns, tableName, sql_connection)

    fhirColumns
      .filter(r => SqlUtils.isAlphaNumeric(r.name) && r.columnNamesOfParentResources.forall(SqlUtils.isAlphaNumeric))
      .filter(r => !isColumnEmpty(r, columnCountResults))
      .map(FhirPropertyToSqlColumn.convert)
  }

  private def checkColumnCounts(fhirColumns: Set[FhirResourceProperty],
                                tableName: String,
                                sql_connection: Connection): ResultSet = {
    val countColumnsSql: Set[String] = fhirColumns
      .filter(r => SqlUtils.isAlphaNumeric(r.name) && r.columnNamesOfParentResources.forall(SqlUtils.isAlphaNumeric))
      .map(r => "sum(case when "
        + FhirPropertyToSqlColumn.convert(r).replace(s"as ${FhirPropertyToSqlColumn.generateUniqueColumnName(r)}", "")
        + " is not null then 1 else 0 end) as " + FhirPropertyToSqlColumn.generateUniqueColumnName(r))

    val countAllColumnsSql = s"select ${countColumnsSql.mkString(",")} " +
      s"from (select resource from $tableName order by random() desc limit 1000) a"

    logger.debug("SQL to check column counts: " + countAllColumnsSql)

    val statement = sql_connection.createStatement()
    val resultSet: ResultSet = statement.executeQuery(countAllColumnsSql)
    resultSet.next()
    resultSet
  }

  private def isColumnEmpty(fhirColumn: FhirResourceProperty,
                            queryResults: ResultSet): Boolean = {
    queryResults.getInt(FhirPropertyToSqlColumn.generateUniqueColumnName(fhirColumn)) == 0
  }

}
