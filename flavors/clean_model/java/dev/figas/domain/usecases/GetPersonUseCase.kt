package dev.figas.domain.usecases

import dev.figas.domain.models.Person
import dev.figas.domain.repositories.PersonRepoContract
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

open class GetPersonUseCase(private val repoContract: PersonRepoContract) : GetPersonUseCaseContract {

    override fun execute(): Single<Person> =
        repoContract.providePerson()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

}

interface GetPersonUseCaseContract{

    fun execute(): Single<Person>
}