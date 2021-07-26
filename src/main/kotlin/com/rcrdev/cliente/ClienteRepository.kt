package com.rcrdev.cliente

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ClienteRepository: JpaRepository<Cliente, String> {
    fun findByCpf(taxIdNumber: String): Optional<Cliente>
}