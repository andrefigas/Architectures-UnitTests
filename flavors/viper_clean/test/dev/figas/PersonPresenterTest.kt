package dev.figas

import dev.figas.domain.models.Person
import dev.figas.domain.usecases.GetPersonUseCaseContract
import dev.figas.domain.usecases.UpdatePersonUseCaseContract
import dev.figas.interactor.MainInteractor
import dev.figas.interactor.MainInteractorContract
import dev.figas.presenter.PersonPresenter
import dev.figas.route.MainRouterContract
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
    private lateinit var getPersonUseCase: GetPersonUseCaseContract
    private lateinit var updatePersonUseCase: UpdatePersonUseCaseContract
    private lateinit var interactor: MainInteractorContract
    private lateinit var router : MainRouterContract

    //TESTS

    //SUCCESS

    @Test
    fun onFetchPersonSuccess(){
        //given
        setupSuccess()

        //when
        `when`(getPersonUseCase.execute()).thenReturn(anySinglePerson())
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
        verify(router, times(1)).goToSuccess(capture(captor, "anyString()"))
        Assert.assertEquals(captor.value, name)
    }

    //FAIL

    @Test
    fun fetchPersonFail() {
        //given
        setupFail()
        `when`(getPersonUseCase.execute()).thenReturn(anySingleThrowable())

        //when
        presenter.fetchPerson()

        //then
        inOrder(view)
        verify(view, times(1)).showLoading()
        verify(view, times(1)).hideLoading()
        verify(view, times(1)).showPersonNameFail()
    }

    @Test
    fun onUpdatePersonFail() {
        //given
        setupFail()
        val name = "world"
        `when`(updatePersonUseCase.execute(anyPerson())).thenReturn(anySingleThrowable())

        //when
        presenter.injectPerson(name)

        //then
        inOrder(view)
        verify(view, times(1)).showLoading()
        verify(view, times(1)).hideLoading()
        verify(view, times(1)).showSavePersonFail()
    }

    //CANCEL

    @Test
    fun fetchPersonCancel(){
        setupCancel()

        //given
        `when`(getPersonUseCase.execute()).thenReturn(
           anySinglePerson().delay(1, TimeUnit.SECONDS)
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
        `when`(updatePersonUseCase.execute(anyPerson())).thenReturn(
            anySinglePerson().delay(1, TimeUnit.SECONDS)
        )

        //when
        presenter.injectPerson(anyString())

        //then
        verify(view, times(1)).showLoading()
        assert(!presenter.release())
        verifyNoMoreInteractions(view)
    }

    //SETUP

    private fun setupSuccess(){
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        router = mock(MainRouterContract::class.java)
        view = mock(PersonView::class.java)
        getPersonUseCase = mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = mock(UpdatePersonUseCaseContract::class.java)
        interactor = MainInteractor(getPersonUseCase, updatePersonUseCase)
        presenter = PersonPresenter(view, router, interactor)
    }

    private fun setupFail(){
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        router = mock(MainRouterContract::class.java)
        view = mock(PersonView::class.java)
        getPersonUseCase = mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = mock(UpdatePersonUseCaseContract::class.java)
        interactor = MainInteractor(getPersonUseCase, updatePersonUseCase)
        presenter = PersonPresenter(view, router, interactor)
    }

    private fun setupCancel(){
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        router = mock(MainRouterContract::class.java)
        view = mock(PersonView::class.java)
        getPersonUseCase = mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = mock(UpdatePersonUseCaseContract::class.java)
        interactor = MainInteractor(getPersonUseCase, updatePersonUseCase)
        presenter = PersonPresenter(view, router, interactor)
    }

    //UTILITIES

    private fun captureString(captor: ArgumentCaptor<String>): String = captor.capture() ?: ""

    private fun <T> capture(captor: ArgumentCaptor<T>, defaultValue : T): T = captor.capture() ?: defaultValue

    private fun anySingleThrowable() : Single<Person> = Single.error(anyThrowable())

    private fun anyThrowable() = Throwable()

    private fun anySinglePerson() = Single.just(anyPerson())

    private fun anyPerson() = Person(anyString())

    private fun anyString() = ""

}