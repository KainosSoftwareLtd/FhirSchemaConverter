package com.kainos.fhirschemaconverter.persistence

import java.sql.{Connection, DriverManager, ResultSet}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

object SqlUtils extends StrictLogging {

  def tableExists(tableName: String, sqlConnection: Connection): Boolean = {
    val dbm = sqlConnection.getMetaData
    val tables = dbm.getTables(null, null, tableName, null)
    tables.next
  }

  def isAlphaNumeric(s: String) = s.forall((('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9'))
    .toSet.contains(_))

  def getSqlConnection () : Connection  = { 
    //loaded from resources/application.conf if no overrides set in environment
    val conf = ConfigFactory.load
    val hostname = conf.getString("output.db.host")
    val port = conf.getString("output.db.port")
    var database = conf.getString("output.db.database")
    var schema = conf.getString("output.db.schema")
    val username = conf.getString("output.db.username")
    val password = conf.getString("output.db.password")

     if (schema.length() > 0) { 
        database = database.concat("?currentSchema=")
        database = database.concat(schema)
     }

    logger.debug(s"database $database")

    Class.forName("org.postgresql.Driver")
    DriverManager.getConnection(
      s"jdbc:postgresql://$hostname:$port/$database", username, password)

  }

}
