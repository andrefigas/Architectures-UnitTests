package dev.figas.model

import io.reactivex.rxjava3.core.Single

class PersonModel : PersonModelContract{
    override fun providePerson(): Single<Person> = Single.just(Person("hello"))

    override fun injectPerson(person: Person): Single<Person> = Single.just(Person("world"))

}

interface PersonModelContract{

    fun providePerson() : Single<Person>

    fun injectPerson(person : Person) : Single<Person>

}