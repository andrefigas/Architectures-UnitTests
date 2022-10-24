package dev.figas.presenter

import dev.figas.intent.event.PersonEvent
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
import dev.figas.interactor.MainInteractorContract
import dev.figas.route.MainRouterContract
import dev.figas.view.PersonView

class PersonPresenter(private val view : PersonView,
                      private val router: MainRouterContract,
                      private val interactor: MainInteractorContract
) : PersonPresenterContract {

     override fun processIntent(personEvent: PersonEvent){
        when(personEvent){
            is PersonEvent.OnSubmitClicked -> {
                injectPerson(personEvent.name)
            }

            is PersonEvent.OnLoad -> {
                fetchPerson()
            }

            is PersonEvent.OnRelease -> {
                release()
            }
        }
    }

    private fun injectPerson(name : String) {
        view.processPageState(PersonState.Loading)
        interactor.injectPerson(
            name,
            { person ->
                view.processEffect(PersonEffect.OnPersonSaved(person))
                router.goToSuccess(person.name)
            },
            {
                view.processEffect(PersonEffect.OnPersonSavedFailed)
            }
        )

    }

    private fun fetchPerson() {
        view.processPageState(PersonState.Loading)
        interactor.fetchPerson(
            { person ->
                view.processPageState(PersonState.Data(person))
            },
            {
                view.processEffect(PersonEffect.OnFetchPersonFailed)
            })
    }

    private fun release(){
        interactor.release()
    }

}

interface PersonPresenterContract{
    fun processIntent(personIntent: PersonEvent)
}
