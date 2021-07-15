package com.rcrdev.bcb

import com.rcrdev.bcb.enums.KeyType
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class CreatePixKeyRequest(

    @field: NotNull
    val keyType: KeyType,

    @field: NotNull
    @field: Size(min = 0, max = 77)
    val key: String,

    @field: NotNull
    val bankAccount: BankAccount,

    @field: NotNull
    val owner: Owner
) {
}