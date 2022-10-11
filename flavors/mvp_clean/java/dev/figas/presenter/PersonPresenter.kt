package dev.figas.presenter

import dev.figas.domain.models.Person
import dev.figas.domain.usecases.GetPersonUseCase
import dev.figas.domain.usecases.GetPersonUseCaseContract
import dev.figas.domain.usecases.UpdatePersonUseCase
import dev.figas.domain.usecases.UpdatePersonUseCaseContract
import dev.figas.view.PersonView
import io.reactivex.rxjava3.disposables.CompositeDisposable

class PersonPresenter(private val view : PersonView,
                      private val getPersonUseCase: GetPersonUseCaseContract,
                      private val updatePersonUseCase: UpdatePersonUseCaseContract) :
    PersonPresenterContract {


    private val requests = CompositeDisposable()

    override fun injectPerson(name : String) {
        view.showLoading()
        requests.add(
            updatePersonUseCase.execute(Person(name)).subscribe(
                { person ->
                    view.hideLoading()
                    view.showSavedPerson(person.name)
                },
                { throwlable ->
                    view.hideLoading()
                    view.showSavedPersonError()
                }
            )

        )

    }

    override fun fetchPerson() {
        view.showLoading()
        requests.add(
            getPersonUseCase.execute().subscribe(
                { person ->
                    view.hideLoading()
                    view.showPersonName(person.name)
                },
                { throwlable ->
                    view.hideLoading()
                    view.showPersonNameError()
                }
            )
        )
    }

    override fun release() : Boolean{
        val disposed = requests.isDisposed
        requests.dispose()
        return disposed
    }

}

interface PersonPresenterContract{
    fun injectPerson(name: String)
    fun fetchPerson()
    fun release() : Boolean
}
