package dev.figas.intent.vieweffect

import dev.figas.model.Person

sealed class PersonEffect {

    data class OnPersonSaved(@JvmField val person: Person) : PersonEffect()

    object OnPersonSavedFailed : PersonEffect()

    object OnFetchPersonFailed : PersonEffect()

}