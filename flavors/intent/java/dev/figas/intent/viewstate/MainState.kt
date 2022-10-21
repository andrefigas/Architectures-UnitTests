package dev.figas.intent.viewstate

import dev.figas.model.Person

sealed class PersonState {

    object Loading : PersonState()
    data class Data(val user: Person) : PersonState()

}