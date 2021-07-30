package com.rcrdev.chavepix

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, Long> {
    @Query("SELECT COUNT(c) < 1 FROM ChavePix c WHERE chave = :chave")
    fun existsByChave(chave: String) : Boolean

    fun existsByPixId(pixId: String) : Boolean
    fun findByPixIdAndClientId(pixId: String?, clientId: String?) : Optional<ChavePix>
    fun findByChave(chave: String): Optional<ChavePix>
}