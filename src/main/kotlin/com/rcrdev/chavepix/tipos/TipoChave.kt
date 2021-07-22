package com.rcrdev.chavepix.tipos

import com.rcrdev.bcb.enums.KeyType
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoChave {
    CPF {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }

            if(!chave.matches("^[0-9]{11}\$".toRegex())) {
                return false
            }

            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
        override fun defineBcbKeyType(): KeyType { return KeyType.CPF }

    },
    TELEFONE {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }
            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
        override fun defineBcbKeyType(): KeyType { return KeyType.PHONE }
    },
    EMAIL {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }
            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
        override fun defineBcbKeyType(): KeyType { return KeyType.EMAIL }
    },
    ALEATORIA {
        override fun valida(chave: String?) = true //chave.isNullOrBlank() //Não deve ser preenchida //não consegui fazer como fez o Rafael
        override fun defineBcbKeyType(): KeyType { return KeyType.RANDOM }
    };

    abstract fun valida(chave: String?): Boolean

    abstract fun defineBcbKeyType(): KeyType
}