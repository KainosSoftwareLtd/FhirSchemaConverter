package com.kainos.fhirschemaconverter.persistence

import org.scalatest._

class SqlWriterSpec extends FlatSpec with Matchers {
  "The isAlphaNumeric method " should "recognise non-alphanumeric characters" in {
    assert(!SqlUtils.isAlphaNumeric("abc[x]def"))
    assert(SqlUtils.isAlphaNumeric("abc"))
  }
}
