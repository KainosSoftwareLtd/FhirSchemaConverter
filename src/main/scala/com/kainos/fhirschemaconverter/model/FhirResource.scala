package com.kainos.fhirschemaconverter.model

/**
  * Internal representation of a FHIR Resource. Has one or more properties. Typically transformed to a view in DB
  */
final case class FhirResource(id: String, tableName: String, properties: Set[FhirResourceProperty])
