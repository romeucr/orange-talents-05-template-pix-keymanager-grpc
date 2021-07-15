package com.rcrdev.cliente

import com.rcrdev.instituicao.Instituicao
import io.micronaut.core.annotation.Introspected
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Introspected
class Cliente(
    @Id
    val id: String,

    @field: NotBlank @field: Column(nullable = false)
    val nome: String,

    @field: NotBlank @field: Column(nullable = false, unique = true)
    val cpf: String,

    @field: NotNull @ManyToOne
    val instituicao: Instituicao
)