package dev.figas

import androidx.arch.core.executor.ArchTaskExecutor
import dev.figas.interactor.MainInteractor
import dev.figas.model.Person
import dev.figas.model.PersonModelContract
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

class MainInteractorTest {

    private lateinit var model : PersonModelContract
    private lateinit var interactor: MainInteractor

    //SUCCESS

    @Test
    fun fetchPerson(){
        //given
        setupCompleted()

        val expected = Person("hello")
        `when`(model.providePerson()).thenReturn(Single.just(expected))

        //when
        interactor.fetchPerson({ person ->
            //then
            assert(person == expected)
        },{
            assert(false)
        })
    }

    @Test
    fun updatePerson(){
        //given
        setupCompleted()
        val expected = Person("world")

        ArchTaskExecutor.getMainThreadExecutor().execute {
            `when`(model.injectPerson(Person(""))).thenReturn(Single.just(expected))

            //when
            interactor.injectPerson(expected.name, { person ->
                //then
                assert(expected == person)
            },{
                assert(false)
            })
        }

    }

    //FAIL

    @Test
    fun fetchPersonFail(){
        //given
        setupCompleted()
        val expected = Throwable()
        `when`(model.providePerson()).thenReturn(Single.error(expected))

        //when
        interactor.fetchPerson({
            assert(false)
        },{ throwlable ->
            //then
            assert(throwlable == expected)
        })
    }

    @Test
    fun updatePersonFail(){
        //given
        setupCompleted()
        val expected = Throwable()
        `when`(model.injectPerson(Person(""))).thenReturn(Single.error(expected))

        //when
        interactor.injectPerson("", {
            assert(false)
        },{ throwlable ->
            //then
            assert(throwlable == expected)
        })

    }

    //CANCEL

    @Test
    fun fetchPersonCancel(){
        //given
        setupCancel()

        val expected = Person("hello")
        `when`(model.providePerson()).thenReturn(Single.just(expected).delay(1, TimeUnit.SECONDS))

        //when
        interactor.fetchPerson({ person ->
            //then
            assert(false)
        },{
            assert(false)
        })

        //then
        assert(!interactor.release())
    }

    @Test
    fun updatePersonCancel(){
        //given
        setupCompleted()
        val expected = Person("world")

        ArchTaskExecutor.getMainThreadExecutor().execute {
            `when`(model.injectPerson(Person(""))).thenReturn(Single.just(expected).delay(1, TimeUnit.SECONDS))

            //when
            interactor.injectPerson(expected.name, { person ->
                assert(false)
            },{
                assert(false)
            })

            //then
            assert(!interactor.release())
        }

    }

    //SETUP

    fun setupCompleted() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        model = mock(PersonModelContract::class.java)
        interactor = MainInteractor(model)
    }

    fun setupCancel() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        model = mock(PersonModelContract::class.java)
        interactor = MainInteractor(model)
    }

}