package com.rcrdev.bcb

import com.rcrdev.bcb.enums.AccountType
import com.rcrdev.bcb.enums.KeyType
import com.rcrdev.bcb.enums.OwnerType
import io.micronaut.http.HttpResponse
import io.micronaut.validation.Validated
import javax.inject.Singleton

@Validated
@Singleton
class BcbService(private val bcbClient: BcbClient) {

    fun registraChavePix(request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse> {
        val ownerPKR = Owner(
            type = OwnerType.LEGAL_PERSON,
            name = "Ander Castro",
            taxIdNumber = "12345365377"
        )

        val bankAccountPKR = BankAccount(
            branch = "1122",
            accountNumber = "332211",
            accountType = AccountType.CACC,
        )
        val createPKR = CreatePixKeyRequest(
            keyType = KeyType.EMAIL,
            key = "ander@email.com",
            bankAccount = bankAccountPKR,
            owner = ownerPKR
        )

        return bcbClient.createChavePix(createPKR)




    }

    fun deletaChavePix() {

    }
}