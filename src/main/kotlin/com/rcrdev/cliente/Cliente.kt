package com.rcrdev.cliente

import io.micronaut.core.annotation.Introspected
import org.hibernate.validator.constraints.br.CPF
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotBlank

@Entity
@Introspected
class Cliente(

    @Id
    val id: String,

    @field: NotBlank
    @field: Column(nullable = false)
    val nome: String,

    @field: NotBlank
    @field: CPF
    @field: Column(nullable = false)
    val cpf: String
) {

}
