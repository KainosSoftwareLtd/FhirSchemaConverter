package com.kainos.fhirschemaconverter.model

case class FhirResource(id: String, tableName: String, properties: Set[FhirResourceProperty])
