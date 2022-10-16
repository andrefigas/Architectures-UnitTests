package dev.figas.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.figas.model.Person
import dev.figas.model.PersonModelContract
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject

class PersonViewModel(private val model: PersonModelContract) : ViewModel() {

    private val requests = CompositeDisposable()
    private val _data: MutableLiveData<Person> = MutableLiveData<Person>()
    val data: LiveData<Person?> = _data

    val insert : PublishSubject<String> = PublishSubject.create()

    fun injectPerson(name: String) {
        requests.add(
            model.injectPerson(Person(name))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { person ->
                        insert.onNext(person.name)
                    },
                    {
                        insert.onNext("")
                    }
                )
        )

    }

    fun fetchPerson() {
        requests.add(
            model.providePerson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { person ->
                        _data.value = person
                    },
                    {
                       _data.value = null
                    }
                )
        )
    }

    fun release(){
        requests.dispose()
    }

}
