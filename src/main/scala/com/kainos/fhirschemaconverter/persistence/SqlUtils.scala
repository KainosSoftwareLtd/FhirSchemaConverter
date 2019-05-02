package com.kainos.fhirschemaconverter.persistence

import java.sql.Connection

object SqlUtils {

  def tableExists(tableName: String, sqlConnection: Connection): Boolean = {
    val dbm = sqlConnection.getMetaData
    val tables = dbm.getTables(null, null, tableName, null)
    tables.next
  }

  def isAlphaNumeric(s: String) = s.forall((('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9'))
    .toSet.contains(_))
}
