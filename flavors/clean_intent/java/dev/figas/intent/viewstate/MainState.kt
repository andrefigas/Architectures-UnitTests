package dev.figas.intent.viewstate

import dev.figas.domain.models.Person

sealed class PersonState {

    object Loading : PersonState()
    data class Data(val user: Person) : PersonState()

}