package dev.figas.interactor

import dev.figas.domain.models.Person
import dev.figas.domain.usecases.GetPersonUseCaseContract
import dev.figas.domain.usecases.UpdatePersonUseCaseContract
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.Consumer

class MainInteractor( private val getPersonUseCase: GetPersonUseCaseContract,
                        private val updatePersonUseCase: UpdatePersonUseCaseContract) : MainInteractorContract{

    private val requests = CompositeDisposable()

    override fun injectPerson(name: String,
                              onSuccess : Consumer<Person>,
                              onFailure : Consumer<Throwable>) {
        requests.add(
            updatePersonUseCase.execute(Person(name))
                .subscribe(onSuccess, onFailure)
        )
    }

    override fun fetchPerson( onSuccess : Consumer<Person>,
                              onFailure : Consumer<Throwable>) {
        requests.add(
            getPersonUseCase.execute().subscribe(onSuccess, onFailure)
        )
    }

    override fun release() : Boolean {
        val disposed = requests.isDisposed
        requests.dispose()
        return disposed
    }

}

interface MainInteractorContract{

    fun injectPerson(name: String, onSuccess: Consumer<Person>, onFailure: Consumer<Throwable>)
    fun fetchPerson(onSuccess: Consumer<Person>, onFailure: Consumer<Throwable>)
    fun release(): Boolean
}
