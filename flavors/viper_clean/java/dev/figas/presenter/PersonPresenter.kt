package dev.figas.presenter

import dev.figas.interactor.MainInteractorContract
import dev.figas.route.MainRouterContract
import dev.figas.view.PersonView

class PersonPresenter(private val view : PersonView,
                      private val router: MainRouterContract,
                      private val interactor: MainInteractorContract
) :
    PersonPresenterContract {

    override fun injectPerson(name : String) {
        view.showLoading()
        interactor.injectPerson(
            name,
            { person ->
                view.hideLoading()
                router.goToSuccess(person.name)
            },
            {
                view.hideLoading()
                view.showPersonNameFail()
            }
        )
    }

    override fun fetchPerson() {
        view.showLoading()
        interactor.fetchPerson(
            { person ->
                view.hideLoading()
                view.showPersonName(person.name)
            },
            { person->
                view.hideLoading()
                view.showPersonNameFail()
            })
    }

    override fun release() = interactor.release()

}

interface PersonPresenterContract{
    fun injectPerson(name: String)
    fun fetchPerson()
    fun release() : Boolean
}
