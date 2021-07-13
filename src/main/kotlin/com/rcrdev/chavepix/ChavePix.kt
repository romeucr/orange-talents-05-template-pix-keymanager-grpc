package com.rcrdev.chavepix

import com.rcrdev.chavepix.tipos.TipoChave
import com.rcrdev.chavepix.tipos.TipoConta
import com.rcrdev.chavepix.validadores.ValidPixKey
import com.rcrdev.chavepix.validadores.ValorUnico
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@ValidPixKey
@Entity
@Introspected
class ChavePix(
    @field: NotBlank
    @field: ValorUnico
    @Column(nullable = false, unique = true)
    val clientId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @field: Size(max = 77)
    @Column(unique = true)
    val chave: String,

    @field: NotBlank
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoConta
) {

    @Id
    @GeneratedValue
    val id: Long? = null

    @Column(nullable = false, unique = true)
    val pixId: String = UUID.randomUUID().toString()
}