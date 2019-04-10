package com.kainos.fhirschemaconverter.model

sealed trait DataType {def sqlType: String}
case object StringType extends DataType {val sqlType = "TEXT"}
case object ArrayType extends DataType {val sqlType = "JSONB"}
case object DateType extends DataType {val sqlType = "DATE"}
case object DoubleType extends DataType {val sqlType = "DOUBLE PRECISION"}