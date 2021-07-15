package com.rcrdev.bcb

import com.rcrdev.bcb.enums.OwnerType
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class Owner(

    @field: NotNull
    val type: OwnerType,

    @field: NotEmpty
    val name: String,

    @field: NotEmpty
    val taxIdNumber: String
) {

}
