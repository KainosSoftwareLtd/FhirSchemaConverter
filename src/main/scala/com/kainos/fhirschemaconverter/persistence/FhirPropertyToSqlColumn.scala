package com.kainos.fhirschemaconverter.persistence

import com.kainos.fhirschemaconverter.model.{ArrayType, FhirResourceProperty, StringType}

/**
  * Converts internal representation of FHIR property to a sql view select column
  * Specific to FhirBase (Postgres)
  */
object FhirPropertyToSqlColumn {
  def convert(fhirColumn: FhirResourceProperty): String = {

    val hasParents = fhirColumn.columnNamesOfParentResources.nonEmpty
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

  def generateUniqueColumnName(fhirColumn: FhirResourceProperty): String = {
    val hasParents = fhirColumn.columnNamesOfParentResources.nonEmpty
    var colName = if (hasParents) fhirColumn.columnNamesOfParentResources.mkString("_") + "_" +
      fhirColumn.name else fhirColumn.name
    
    if (colName.length() > 62) {
      // shorten column name by removing the _
      colName = colName.replace("_","")

      // if still too long truncate it.
      if (colName.length() > 62) {
            colName.slice(0, 61) //postgres max length of column name is 62
      }
      else {
        colName
      }
    }
    else {
      colName
    }
  }
}
