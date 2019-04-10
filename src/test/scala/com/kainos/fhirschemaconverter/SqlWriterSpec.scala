package com.kainos.fhirschemaconverter

import org.scalatest._

class SqlWriterSpec extends FlatSpec with Matchers {
  "The isAlphaNumeric method " should "recognise non-alphanumeric characters" in {
    assert(!SqlWriter.isAlphaNumeric("abc[x]def"))
    assert(SqlWriter.isAlphaNumeric("abc"))
  }
}
