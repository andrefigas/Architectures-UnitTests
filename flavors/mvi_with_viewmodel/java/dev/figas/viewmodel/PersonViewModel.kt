package dev.figas.viewmodel

import android.os.AsyncTask
import androidx.lifecycle.*
import dev.figas.intent.event.PersonEvent
import dev.figas.model.Person
import dev.figas.model.PersonModelContract
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
import io.reactivex.rxjava3.subjects.PublishSubject


class PersonViewModel(val model: PersonModelContract) : ViewModel() {

    private val _uiState : MutableLiveData<PersonState> = MutableLiveData(PersonState.Idle)
    val uiState : LiveData<PersonState> = _uiState

    val effetcs: PublishSubject<PersonEffect> = PublishSubject.create()

    private val requests = mutableListOf<AsyncTask<*, *, *>>()

    fun sendIntent(event : PersonEvent) {
        when (event) {
            is PersonEvent.OnSubmitClicked -> {
                injectPerson(event.name)
            }

            is PersonEvent.OnLoad -> {
                fetchPerson()
            }
        }
    }

    private fun injectPerson(name: String) {
        requests.add(
            model.injectPerson(Person(name), onPreExecute = {
                _uiState.value = PersonState.Loading
            }, onPostExecute = { person ->
                effetcs.onNext(PersonEffect.OnPersonSaved(person))
            })
        )

    }

    private fun fetchPerson() {
        requests.add(
            model.providePerson(onPreExecute = {
                _uiState.value = PersonState.Loading
            }, onPostExecute = { person ->
                _uiState.value = PersonState.Data(person)
            })
        )

    }

    override fun onCleared() {
        super.onCleared()
        requests.forEach {
            it.cancel(true)
        }

    }

}
