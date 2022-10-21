package dev.figas.interactor

import dev.figas.model.Person
import dev.figas.model.PersonModelContract
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers

class MainInteractor( private val model: PersonModelContract) : MainInteractorContract{

    private val requests = CompositeDisposable()

    override fun injectPerson(name: String,
                              onSuccess : Consumer<Person>,
                              onFailure : Consumer<Throwable>) {
        requests.add(
            model.injectPerson(Person(name))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
                .subscribe(onSuccess, onFailure)
        )
    }

    override fun fetchPerson(onSuccess : Consumer<Person>,
                             onFailure : Consumer<Throwable>) {
        requests.add(
            model.providePerson()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(onSuccess, onFailure)
        )
    }

    override fun release() : Boolean {
        val disposed = requests.isDisposed
        requests.dispose()
        return disposed
    }

}

interface MainInteractorContract{

    fun release() : Boolean
    fun injectPerson(name: String, onSuccess: Consumer<Person>, onFailure: Consumer<Throwable>)
    fun fetchPerson(onSuccess: Consumer<Person>, onFailure: Consumer<Throwable>)
}
