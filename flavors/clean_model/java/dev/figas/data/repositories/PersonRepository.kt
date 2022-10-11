package dev.figas.data.repositories

import dev.figas.data.mappers.PersonMapperContract
import dev.figas.data.models.PersonDataModel
import dev.figas.domain.models.Person
import dev.figas.domain.repositories.PersonRepoContract
import io.reactivex.rxjava3.core.Single

class PersonRepository(private val mapper: PersonMapperContract) : PersonRepoContract{

    override fun providePerson(): Single<Person> = Single.just(PersonDataModel("hello")).map {
        mapper.toPerson(it)
    }

    override fun injectPerson(person: Person): Single<Person> = Single.just(PersonDataModel("world")).map {
        mapper.toPerson(it)
    }
}
