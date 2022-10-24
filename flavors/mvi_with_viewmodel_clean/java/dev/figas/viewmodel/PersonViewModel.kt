package dev.figas.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import dev.figas.domain.models.Person
import dev.figas.domain.usecases.GetPersonUseCaseContract
import dev.figas.domain.usecases.UpdatePersonUseCaseContract
import dev.figas.intent.event.PersonEvent
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject

class PersonViewModel(private val getPersonUseCase: GetPersonUseCaseContract,
                      private val updatePersonUseCase: UpdatePersonUseCaseContract
) : ViewModel() {

    private val _uiState : MutableLiveData<PersonState> = MutableLiveData()
    val uiState : LiveData<PersonState> = _uiState

    val effect : PublishSubject<PersonEffect> = PublishSubject.create()

    private val requests = CompositeDisposable()

    fun sendIntent(event : PersonEvent) {
        when (event) {
            is PersonEvent.OnSubmitClicked -> {
                injectPerson(event.name)
            }

            is PersonEvent.OnLoad -> {
                fetchPerson()
            }

            PersonEvent.OnRelease -> release()
        }
    }

    private fun injectPerson(name: String) {
        _uiState.value = PersonState.Loading
        requests.add(
            updatePersonUseCase.execute(Person(name)).subscribe(
                { person ->
                    effect.onNext(PersonEffect.OnPersonSaved(person))
                },{
                    effect.onNext(PersonEffect.OnPersonSavedFailed)
                }
            )
        )

    }

    private fun fetchPerson() {
        _uiState.value = PersonState.Loading
        requests.add(
            getPersonUseCase.execute().subscribe({ person ->
                _uiState.value = PersonState.Data(person)
            }, {
                effect.onNext(PersonEffect.OnFetchPersonFailed)
            })
        )

    }

   fun release(){
       requests.dispose()
   }

}
