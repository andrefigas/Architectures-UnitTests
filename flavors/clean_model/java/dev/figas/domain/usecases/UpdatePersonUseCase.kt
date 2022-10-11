package dev.figas.domain.usecases

import dev.figas.domain.models.Person
import dev.figas.domain.repositories.PersonRepoContract
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

open class UpdatePersonUseCase(private val repoContract: PersonRepoContract) : UpdatePersonUseCaseContract{

    override fun execute(person: Person): Single<Person> =
        repoContract.injectPerson(person)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())

}

interface UpdatePersonUseCaseContract{

    fun execute(person: Person): Single<Person>
}