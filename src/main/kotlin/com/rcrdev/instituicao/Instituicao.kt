package com.rcrdev.instituicao

import io.micronaut.core.annotation.Introspected
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotBlank

@Entity
@Introspected
class Instituicao(

    @Id
    val ispb: Int,

    @field: NotBlank
    @field: Column(nullable = false)
    val nome: String,

) {

}