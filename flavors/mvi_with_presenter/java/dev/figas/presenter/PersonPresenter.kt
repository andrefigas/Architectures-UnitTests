package dev.figas.presenter

import dev.figas.intent.event.PersonEvent
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
import dev.figas.model.Person
import dev.figas.model.PersonModelContract
import dev.figas.view.PersonView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class PersonPresenter(private val view: PersonView, private val model: PersonModelContract) :
    PersonPresenterContract {

    private val requests = CompositeDisposable()

    override fun processIntent(personIntent: PersonEvent) {

        when (personIntent) {

            is PersonEvent.OnLoad -> {
                fetchPerson()
            }

            is PersonEvent.OnSubmitClicked -> {
                injectPerson(personIntent.name)
            }

            is PersonEvent.OnRelease -> {
                release()
            }


        }
    }

    private fun injectPerson(name: String) {
        view.processPageState(PersonState.Loading)
        requests.add(
            model.injectPerson(
                Person(name)
            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ person ->
                    view.processEffect(PersonEffect.OnPersonSaved(person))
                }, {
                    view.processEffect(PersonEffect.OnPersonSavedFailed)
                }
                )

        )

    }

    private fun fetchPerson() {
        view.processPageState(PersonState.Loading)
        requests.add(
            model.providePerson()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe({ person ->
                    view.processPageState(PersonState.Data(person))
                }, {
                    view.processEffect(PersonEffect.OnFetchPersonFailed)
                })
        )
    }

    private fun release() {
        requests.dispose()
    }

}

interface PersonPresenterContract {
    fun processIntent(personIntent: PersonEvent)
}
