package com.rcrdev.bcb

import com.rcrdev.bcb.enums.KeyType
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Client("\${bcb.database.url}")
interface BcbClient {

    @Post
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun createChavePix(@Body createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete ("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun deleteChavePix(@Body deletePixKeyRequest: DeletePixKeyRequest, key: String): HttpResponse<DeletePixKeyResponse>

    @Get ("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    fun getChavePix(key: String): HttpResponse<PixKeyDetailsResponse>
}

@Introspected
data class DeletePixKeyRequest(val key: String, val participant: String)

@Introspected
data class DeletePixKeyResponse(val key: String, val participant: String)

@Introspected
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
    /* overrride no equal e hashcode para que não leve em consideração o valor da chave(key)
     * deixando o key, o teste deve atualizar o valor da chave quando for do tipo ALEATORIA falha */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreatePixKeyRequest

        if (keyType != other.keyType) return false
        if (bankAccount != other.bankAccount) return false
        if (owner != other.owner) return false

        return true
    }
    override fun hashCode(): Int {
        var result = keyType.hashCode()
        result = 31 * result + bankAccount.hashCode()
        result = 31 * result + owner.hashCode()
        return result
    }
}

@Introspected
data class CreatePixKeyResponse(

    @field: NotNull
    val keyType: KeyType,

    @field: NotBlank
    val key: String,

    @field: NotNull
    val bankAccount: BankAccount,

    @field: NotNull
    val owner: Owner,

    @field: NotBlank
    val createdAt: LocalDateTime
)

data class PixKeyDetailsResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)