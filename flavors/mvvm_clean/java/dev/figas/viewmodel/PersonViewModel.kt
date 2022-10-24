package dev.figas.viewmodel

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.figas.domain.models.Person
import dev.figas.domain.usecases.GetPersonUseCase
import dev.figas.domain.usecases.GetPersonUseCaseContract
import dev.figas.domain.usecases.UpdatePersonUseCase
import dev.figas.domain.usecases.UpdatePersonUseCaseContract
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject

class PersonViewModel(private val getPersonUseCase: GetPersonUseCaseContract,
                      private val updatePersonUseCase: UpdatePersonUseCaseContract) : ViewModel() {

    private val requests = CompositeDisposable()
    private val _data: MutableLiveData<Person> = MutableLiveData<Person>()
    val data: LiveData<Person> = _data

    val insert: PublishSubject<String> = PublishSubject.create()

    fun injectPerson(name: String) {
        requests.add(
            updatePersonUseCase.execute(Person(name)).subscribe({ person ->
                insert.onNext(person.name)
            },{
                insert.onNext("")
            })
        )

    }

    fun fetchPerson() {
        requests.add(
            getPersonUseCase.execute().subscribe({ person ->
                _data.value = person
            }, {
                _data.value = null
            })
        )
    }

    fun release(){
        requests.dispose()
    }

}
