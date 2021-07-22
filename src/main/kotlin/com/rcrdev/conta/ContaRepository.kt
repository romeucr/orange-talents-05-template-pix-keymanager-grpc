package com.rcrdev.conta

import com.rcrdev.chavepix.tipos.TipoConta
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ContaRepository: JpaRepository<Conta, Long> {

    @Query("SELECT COUNT(c) > 0 FROM Conta c WHERE agencia = :agencia AND numero = :numero AND tipo_conta = :tipo")
    fun existsByAgenciaNumeroTipo(agencia: String, numero: String, tipo:String): Boolean

    @Query("SELECT c FROM Conta c WHERE titular_id = :clientId AND tipo_conta = :tipoConta")
    fun findByClientIdAndTipoConta(clientId: String, tipoConta: String): Optional<Conta>

}