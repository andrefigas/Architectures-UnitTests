package dev.figas.presenter

import dev.figas.model.Person
import dev.figas.model.PersonModelContract
import dev.figas.view.PersonView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers

class PersonPresenter(private val view : PersonView, private val model: PersonModelContract) : PersonPresenterContract {


    private val requests = CompositeDisposable()

    override fun injectPerson(name : String) {
        view.showLoading()

        requests.add(
            model.injectPerson(Person(name))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { person ->
                        view.hideLoading()
                        view.showSavedPerson(person.name)
                    },
                    { error ->
                        view.hideLoading()
                        view.showSavedPersonError()
                    }

                )
        )

    }

    override fun fetchPerson() {
        view.showLoading()
        requests.add(
            model.providePerson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { person ->
                        view.hideLoading()
                        view.showPersonName(person.name)
                    },
                    { error ->
                        view.hideLoading()
                        view.showPersonNameError()
                    }

                )
        )
    }

    override fun release(): Boolean {
        val disposed = requests.isDisposed
        requests.dispose()
        return disposed
    }

}

interface PersonPresenterContract{
    fun injectPerson(name: String)
    fun fetchPerson()
    fun release() : Boolean
}
