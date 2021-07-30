package com.rcrdev.chavepix

import com.rcrdev.chavepix.tipos.TipoChave
import com.rcrdev.chavepix.tipos.TipoConta
import com.rcrdev.chavepix.validadores.ValidPixKey
import com.rcrdev.chavepix.validadores.ValorUnico
import io.micronaut.core.annotation.Introspected
import jdk.jfr.Timestamp
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Entity
@Introspected
class ChavePix(
    @field: NotBlank
    @Column(nullable = false)
    val clientId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @field: Size(max = 77)
    @field: ValorUnico
    @Column(unique = true)
    var chave: String,

    @field: NotBlank
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoConta,
) {

    @Id
    @GeneratedValue
    val id: Long? = null

    @Column(nullable = false, unique = true)
    var pixId: String = UUID.randomUUID().toString()

    var criadoEm: LocalDateTime? = null
}