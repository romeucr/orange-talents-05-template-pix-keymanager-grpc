package com.rcrdev.bcb

import com.rcrdev.bcb.enums.AccountType
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class BankAccount(

    @field: Size(min = 4, max = 4)
    val branch: String, //agencia sem digito

    @field: Size(min = 6, max = 6)
    val accountNumber: String, //conta com digito. Se digito for letra, substituir por zero

    @field: NotNull
    val accountType: AccountType
) {
    @field: NotEmpty
    val participant = "60701190" // ISPB do ITAU
}
