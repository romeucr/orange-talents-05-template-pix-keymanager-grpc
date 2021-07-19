package com.rcrdev.conta

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ContaRepository: JpaRepository<Conta, Long> {

    @Query("SELECT COUNT(c) > 0 FROM Conta c WHERE agencia = :agencia AND numero = :numero")
    fun existsByAgenciaAndNumero(agencia: String, numero: String): Boolean

}