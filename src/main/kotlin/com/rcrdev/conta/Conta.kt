package com.rcrdev.conta

import com.rcrdev.chavepix.tipos.TipoConta
import com.rcrdev.cliente.Cliente
import com.rcrdev.instituicao.Instituicao
import io.micronaut.core.annotation.Introspected
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Introspected
class Conta(

    @field: NotNull
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta,

    @field: NotNull
    @ManyToOne
    val instituicao: Instituicao,

    @field: NotBlank
    val agencia: String,

    @field: NotBlank
    val numero: String,

    @field: NotNull
    @OneToOne
    val titular: Cliente
) {
    @Id
    @GeneratedValue
    val id: Long? = null
}