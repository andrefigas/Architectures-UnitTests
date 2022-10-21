package dev.figas.viewmodel

import androidx.lifecycle.*
import dev.figas.intent.event.PersonEvent
import dev.figas.model.Person
import dev.figas.model.PersonModelContract
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject


class PersonViewModel(private val model: PersonModelContract) : ViewModel() {

    private val _uiState : MutableLiveData<PersonState> = MutableLiveData()
    val uiState : LiveData<PersonState> = _uiState

    val effects: PublishSubject<PersonEffect> = PublishSubject.create()

    private val requests = CompositeDisposable()

    fun sendIntent(event : PersonEvent) {
        when (event) {
            is PersonEvent.OnSubmitClicked -> {
                injectPerson(event.name)
            }

            is PersonEvent.OnLoad -> {
                fetchPerson()
            }

            PersonEvent.OnRelease -> {
                release()
            }
        }
    }

    private fun injectPerson(name: String) {
        _uiState.value = PersonState.Loading
        requests.add(
            model.injectPerson(Person(name))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ person ->
                    effects.onNext(PersonEffect.OnPersonSaved(person))
                }, {
                    effects.onNext(PersonEffect.OnPersonSavedFailed)
                })
        )

    }

    private fun fetchPerson() {
        _uiState.value = PersonState.Loading
        requests.add(
            model.providePerson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ person ->
                    _uiState.value = PersonState.Data(person)
                }, {
                    effects.onNext(PersonEffect.OnFetchPersonFailed)
                })
        )

    }

    override fun onCleared() {
        super.onCleared()
        release()
    }

    private fun release(){
        requests.dispose()
    }

}
