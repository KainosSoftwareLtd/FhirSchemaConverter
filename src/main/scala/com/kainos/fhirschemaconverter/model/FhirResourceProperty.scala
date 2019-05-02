package com.kainos.fhirschemaconverter.model

/**
  * Internal representation of a FHIR Resource Property. Typically transformed to a column in DB view
  */
final case class FhirResourceProperty(name: String, dataType: DataType, columnNamesOfParentResources: List[String])


