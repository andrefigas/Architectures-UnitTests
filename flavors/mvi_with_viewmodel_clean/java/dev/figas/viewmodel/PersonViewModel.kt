package dev.figas.viewmodel

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import dev.figas.domain.models.Person
import dev.figas.domain.usecases.GetPersonUseCase
import dev.figas.domain.usecases.UpdatePersonUseCase
import dev.figas.intent.event.PersonEvent
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
import io.reactivex.rxjava3.subjects.PublishSubject

class PersonViewModel(private val getPersonUseCase: GetPersonUseCase,
                      private val updatePersonUseCase: UpdatePersonUseCase
) : ViewModel() {

    private val _uiState : MutableLiveData<PersonState> = MutableLiveData(PersonState.Idle)
    val uiState : LiveData<PersonState> = _uiState

    val effect : PublishSubject<PersonEffect> = PublishSubject.create()

    private val requests = mutableListOf<AsyncTask<*, *, *>>()

    fun sentIntent(event : PersonEvent) {
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
            updatePersonUseCase.execute(Person(name), onPreExecute = {
                _uiState.value = PersonState.Loading
            }, onPostExecute = { person ->
                effect.onNext(PersonEffect.OnPersonSaved(person))
            })
        )

    }

    private fun fetchPerson() {
        requests.add(
            getPersonUseCase.execute(onPreExecute = {
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
