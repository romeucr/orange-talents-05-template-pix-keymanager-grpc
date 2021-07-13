package com.rcrdev.compartilhado.utils

fun ofuscaUuid(clientId: String?): String {
    return "***-${clientId?.substring(19)}"
}