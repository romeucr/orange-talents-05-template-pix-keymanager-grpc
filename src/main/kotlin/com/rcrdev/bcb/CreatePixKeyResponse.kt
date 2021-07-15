package com.rcrdev.bcb

import com.rcrdev.bcb.enums.KeyType
import java.time.LocalDateTime

data class CreatePixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {
}