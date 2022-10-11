package dev.figas

import dev.figas.data.mappers.PersonMapper
import dev.figas.data.mappers.PersonMapperContract
import dev.figas.data.repositories.PersonRepository
import dev.figas.domain.models.Person
import dev.figas.domain.usecases.GetPersonUseCase
import dev.figas.domain.usecases.GetPersonUseCaseContract
import dev.figas.domain.usecases.UpdatePersonUseCase
import dev.figas.domain.usecases.UpdatePersonUseCaseContract
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

    private lateinit var getPersonUseCase : GetPersonUseCaseContract
    private lateinit var updatePersonUseCase: UpdatePersonUseCaseContract
    private lateinit var mapper: PersonMapperContract
    private lateinit var repo: PersonRepository
    private lateinit var view : PersonView
    private lateinit var presenter: PersonPresenter

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
        `when`(getPersonUseCase.execute()).thenReturn(Single.error(Throwable()))

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
        `when`(updatePersonUseCase.execute(Person(name))).thenReturn(Single.error(Throwable()))

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
        `when`(getPersonUseCase.execute()).thenReturn(
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
        `when`(updatePersonUseCase.execute(Person(""))).thenReturn(
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
        mapper = PersonMapper()
        repo = PersonRepository(mapper)
        getPersonUseCase = GetPersonUseCase(repo)
        updatePersonUseCase = UpdatePersonUseCase(repo)
        presenter = PersonPresenter(view, getPersonUseCase, updatePersonUseCase)
    }

    private fun setupFail(){
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        view = mock(PersonView::class.java)
        mapper = PersonMapper()
        repo = PersonRepository(mapper)
        getPersonUseCase = mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = mock(UpdatePersonUseCase::class.java)
        presenter = PersonPresenter(view, getPersonUseCase, updatePersonUseCase)
    }

    private fun setupCancel(){
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        view = mock(PersonView::class.java)
        mapper = PersonMapper()
        repo = PersonRepository(mapper)
        getPersonUseCase = mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = mock(UpdatePersonUseCase::class.java)
        presenter = PersonPresenter(view, getPersonUseCase, updatePersonUseCase)
    }

    //UTILITIES

    private fun captureString(captor: ArgumentCaptor<String>): String = captor.capture() ?: ""

    private fun <T> capture(captor: ArgumentCaptor<T>, defaultValue : T): T = captor.capture() ?: defaultValue

}