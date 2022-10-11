package dev.figas.data.mappers

import dev.figas.data.models.PersonDataModel
import dev.figas.domain.models.Person

class PersonMapper : PersonMapperContract{

    override fun toPerson(personDataModel: PersonDataModel) = Person(personDataModel.name)

}

interface PersonMapperContract{

    fun toPerson(personDataModel: PersonDataModel): Person
}