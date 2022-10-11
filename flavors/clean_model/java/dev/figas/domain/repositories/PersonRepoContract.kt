package dev.figas.domain.repositories

import android.os.AsyncTask
import dev.figas.domain.models.Person
import io.reactivex.rxjava3.core.Single

interface PersonRepoContract {

    fun providePerson() : Single<Person>

    fun injectPerson(person : Person) : Single<Person>

}