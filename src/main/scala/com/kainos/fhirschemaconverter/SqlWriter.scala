package com.kainos.fhirschemaconverter

import java.sql.{Connection, DriverManager, ResultSet}

import com.kainos.fhirschemaconverter.model._

object SqlWriter {

  def createSqlViews(fhirResources: Set[FhirResource]): Unit = {

    Class.forName("org.postgresql.Driver")
    val sqlConnection: Connection = DriverManager.getConnection(
      "jdbc:postgresql://localhost/fhirbase", "postgres", "postgres")

    fhirResources.filter(
      r => tableExists(r.tableName.toLowerCase, sqlConnection))
      .foreach(fhirResource => {
        val tableName = fhirResource.tableName.toLowerCase()
        val viewName = s"${fhirResource.id.replace("-", "_")}_view"
        val sqlColumns = convertFhirColumnsToSqlColumns(fhirResource.properties, tableName, sqlConnection)
        val sqlCreateView = s"drop view if exists $viewName; create or replace view $viewName as select " +
          sqlColumns.mkString(",") +
          s" from $tableName a"

        println(s"Full SQL to create view: $sqlCreateView")

        if (sqlColumns.size > 0 && tableExists(tableName, sqlConnection)) {
          val prepare_statement = sqlConnection.prepareStatement(sqlCreateView)
          prepare_statement.executeUpdate()
          prepare_statement.close()
        }
      }
      )
  }

  private def tableExists(tableName: String, sqlConnection: Connection): Boolean = {
    val dbm = sqlConnection.getMetaData
    val tables = dbm.getTables(null, null, tableName, null)
    tables.next
  }

  private def convertFhirColumnsToSqlColumns(fhirColumns: Set[FhirResourceProperty], tableName: String,
                                             sql_connection: Connection): Set[String] = {

    val columnCountResults: ResultSet = checkColumnCounts(fhirColumns, tableName, sql_connection)

    fhirColumns
      .filter(r => isAlphaNumeric(r.name) && r.columnNamesOfParentResources.forall(isAlphaNumeric))
      .filter(r => !isColumnEmpty(r, columnCountResults))
      .map(fhirColumnToSqlColumn)
  }

  private def checkColumnCounts(fhirColumns: Set[FhirResourceProperty],
                                tableName: String, sql_connection: Connection) = {
    val countColumnsSql: Set[String] = fhirColumns
      .filter(r => isAlphaNumeric(r.name) && r.columnNamesOfParentResources.forall(isAlphaNumeric))
      .map(r => "sum(case when "
        + fhirColumnToSqlColumn(r).replace(s"as ${generateUniqueColumnName(r)}", "")
        + " is not null then 1 else 0 end) as " + generateUniqueColumnName(r))

    val countAllColumnsSql = s"select ${countColumnsSql.mkString(",")} " +
      s"from (select resource from $tableName order by random() desc limit 1000) a"

    println("Sql to check column counts: " + countAllColumnsSql)

    val statement = sql_connection.createStatement()
    val resultSet: ResultSet = statement.executeQuery(countAllColumnsSql)
    resultSet.next()
    resultSet
  }

  private def isColumnEmpty(fhirColumn: FhirResourceProperty, queryResults: ResultSet): Boolean = {
    queryResults.getInt(generateUniqueColumnName(fhirColumn)) == 0
  }

  private def fhirColumnToSqlColumn(fhirColumn: FhirResourceProperty): String = {

    val hasParents = !fhirColumn.columnNamesOfParentResources.isEmpty
    val parentSelector = if (hasParents) {
      ("->'" + fhirColumn.columnNamesOfParentResources.mkString("'->'") + "' ->>'")
        .replace(">'0'", ">0")
    } else "->>'"
    val asClause = "as " + generateUniqueColumnName(fhirColumn)

    if (fhirColumn.dataType.equals(ArrayType)) {
      "cast(a.resource" + parentSelector.replace("->>'", "->'") +
        fhirColumn.name + "' as " + fhirColumn.dataType.sqlType + ")" + asClause
    }
    else if (!fhirColumn.dataType.equals(StringType)) {
      "cast(a.resource" + parentSelector + fhirColumn.name + "' as " +
        fhirColumn.dataType.sqlType + ") " + asClause
    }
    else {
      "a.resource" + parentSelector + fhirColumn.name + "' " + asClause
    }
  }

  private def generateUniqueColumnName(fhirColumn: FhirResourceProperty): String = {
    val hasParents = !fhirColumn.columnNamesOfParentResources.isEmpty
    val colName = if (hasParents) fhirColumn.columnNamesOfParentResources.mkString("_") + "_" +
      fhirColumn.name else fhirColumn.name
    colName.slice(0, 60) //postgres max length of column name is 62
  }

  def isAlphaNumeric(s: String) = s.forall((('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9'))
    .toSet.contains(_))

}
