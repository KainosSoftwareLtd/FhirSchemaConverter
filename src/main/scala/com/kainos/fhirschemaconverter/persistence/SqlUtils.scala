package com.kainos.fhirschemaconverter.persistence

import java.sql.Connection

object SqlUtils {

  def tableExists(tableName: String, sqlConnection: Connection): Boolean = {
    val dbm = sqlConnection.getMetaData
    val tables = dbm.getTables(null, null, tableName, null)
    tables.next
  }
}
