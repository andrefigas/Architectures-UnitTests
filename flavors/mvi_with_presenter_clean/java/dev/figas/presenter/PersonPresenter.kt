package dev.figas.presenter

import dev.figas.domain.models.Person
import dev.figas.domain.usecases.GetPersonUseCaseContract
import dev.figas.domain.usecases.UpdatePersonUseCaseContract
import dev.figas.intent.event.PersonEvent
import dev.figas.view.PersonView
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
import io.reactivex.rxjava3.disposables.CompositeDisposable

class PersonPresenter(private val view : PersonView,
                      private val getPersonUseCase: GetPersonUseCaseContract,
                      private val updatePersonUseCase: UpdatePersonUseCaseContract
) : PersonPresenterContract {

    private val requests = CompositeDisposable()

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
        requests.add(
            updatePersonUseCase.execute(Person(name)).subscribe(
                { person->
                    view.processEffect(PersonEffect.OnPersonSaved(person))
                },
                {
                    view.processEffect(PersonEffect.OnPersonSavedFailed)
                }
            )

        )

    }

    private fun fetchPerson() {
        view.processPageState(PersonState.Loading)
        requests.add(
            getPersonUseCase.execute().subscribe(
                { person->
                    view.processPageState(PersonState.Data(person))
                }, {
                    view.processEffect(PersonEffect.OnFetchPersonFailed)
                })
        )
    }

    private fun release(){
        requests.dispose()
    }

}

interface PersonPresenterContract{
    fun processIntent(personIntent: PersonEvent)
}
