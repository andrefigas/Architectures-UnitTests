package dev.figas

import dev.figas.model.Person
import dev.figas.model.PersonModel

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class PersonModelTest {

    lateinit var model : PersonModel

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        model = PersonModel()
    }

    @Test
    fun fetchPerson(){
        //when
        model.providePerson().test()
            //then
            .assertValue { person ->
            person.name == "hello"
        }
    }

    @Test
    fun updatePerson(){
        //given
        val name = "world"

        //when
        model.injectPerson(Person(name)).test()
            //then
            .assertValue { person ->
            person.name == name
        }
    }

}