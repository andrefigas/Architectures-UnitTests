package dev.figas

import dev.figas.model.Person
import dev.figas.model.PersonModel
import dev.figas.model.PersonModelContract
import dev.figas.presenter.PersonPresenter
import dev.figas.view.PersonView
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.util.concurrent.TimeUnit

class PersonPresenterTest {

    private lateinit var view : PersonView
    private lateinit var presenter : PersonPresenter
    private lateinit var model : PersonModelContract

    //TESTS

    //SUCCESS

    @Test
    fun onFetchPersonSuccess(){
        //given
        setupSuccess()

        //when
        presenter.fetchPerson()

        //then
        val captor  = ArgumentCaptor.forClass(String::class.java)
        inOrder(view)
        verify(view, times(1)).showLoading()
        verify(view, times(1)).hideLoading()
        verify(view, times(1)).showPersonName(captureString(captor))
        Assert.assertEquals(captor.value, "hello")
    }

    @Test
    fun onUpdatePersonSuccess(){
        //given
        setupSuccess()
        val name = "world"

        //when
        presenter.injectPerson(name)

        //then
        val captor  = ArgumentCaptor.forClass(String::class.java)
        inOrder(view)
        verify(view, times(1)).showLoading()
        verify(view, times(1)).hideLoading()
        verify(view, times(1)).showSavedPerson(capture(captor, "anyString()"))
        Assert.assertEquals(captor.value, name)
    }

    //FAIL

    @Test
    fun fetchPersonFail() {
        //given
        setupFail()
        `when`(model.providePerson()).thenReturn(Single.error(Throwable()))

        //when
        presenter.fetchPerson()

        //then
        inOrder(view)
        verify(view, times(1)).showLoading()
        verify(view, times(1)).hideLoading()
        verify(view, times(1)).showPersonNameError()
    }

    @Test
    fun onUpdatePersonFail() {
        //given
        setupFail()
        val name = "world"
        `when`(model.injectPerson(Person(name))).thenReturn(Single.error(Throwable()))

        //when
        presenter.injectPerson(name)

        //then
        inOrder(view)
        verify(view, times(1)).showLoading()
        verify(view, times(1)).hideLoading()
        verify(view, times(1)).showSavedPersonError()
    }

    //CANCEL

    @Test
    fun fetchPersonCancel(){
        setupCancel()

        //given
        `when`(model.providePerson()).thenReturn(
            Single.just(Person("")).delay(1, TimeUnit.SECONDS)
        )

        //when
        presenter.fetchPerson()

        //then
        verify(view, times(1)).showLoading()
        assert(!presenter.release())
        verifyNoMoreInteractions(view)
    }

    @Test
    fun updatePersonCancel(){
        setupCancel()

        //given
        `when`(model.injectPerson(Person(""))).thenReturn(
            Single.just(Person("")).delay(1, TimeUnit.SECONDS)
        )

        //when
        presenter.injectPerson("")

        //then
        verify(view, times(1)).showLoading()
        assert(!presenter.release())
        verifyNoMoreInteractions(view)
    }

    //SETUP

    private fun setupSuccess(){
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        view = mock(PersonView::class.java)
        model = PersonModel()
        presenter = PersonPresenter(view, model)
    }

    private fun setupFail(){
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        view = mock(PersonView::class.java)
        model = mock(PersonModelContract::class.java)
        presenter = PersonPresenter(view, model)
    }

    private fun setupCancel(){
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        view = mock(PersonView::class.java)
        model = mock(PersonModelContract::class.java)
        presenter = PersonPresenter(view, model)
    }

    //UTILITIES

    private fun captureString(captor: ArgumentCaptor<String>): String = captor.capture() ?: ""

    private fun <T> capture(captor: ArgumentCaptor<T>, defaultValue : T): T = captor.capture() ?: defaultValue

}