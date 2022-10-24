package dev.figas

import dev.figas.domain.models.Person
import dev.figas.domain.usecases.GetPersonUseCaseContract
import dev.figas.domain.usecases.UpdatePersonUseCaseContract
import dev.figas.intent.event.PersonEvent
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
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
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

class PersonPresenterTest {

    private lateinit var view : PersonView
    private lateinit var presenter : PersonPresenter
    private lateinit var getPersonUseCase: GetPersonUseCaseContract
    private lateinit var updatePersonUseCase: UpdatePersonUseCaseContract
    private lateinit var interactor: MainInteractorContract
    private lateinit var router: MainRouterContract

    //SUCCESS

    @Test
    fun onFetchPersonSuccess(){
        //given
        setupSuccess()

        Mockito.`when`(getPersonUseCase.execute()).thenReturn(Single.just(Person("hello")))

        //when
        presenter.processIntent(PersonEvent.OnLoad)

        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(2))
            .processPageState(stateCaptor.capture()?: anyPageState())

        stateCaptor.allValues.forEachIndexed { index, personState ->
            when(index){
                0 -> Assert.assertEquals(personState, PersonState.Loading)
                1 -> assert(personState is PersonState.Data && personState.user.name == "hello")
                else -> assert(false)
            }
        }

    }

    @Test
    fun onUpdatePersonSuccess(){
        //given
        setupSuccess()
        val name = "world"
        val expectedPerson = Person(name)
        val expectedEffect =  PersonEffect.OnPersonSaved(expectedPerson)

        Mockito.`when`(updatePersonUseCase.execute(anyPerson())).thenReturn(Single.just(expectedPerson))

        //when
        presenter.processIntent(PersonEvent.OnSubmitClicked(anyString()))

        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)
        val effectCaptor : ArgumentCaptor<PersonEffect.OnPersonSaved>  = ArgumentCaptor.forClass(PersonEffect.OnPersonSaved::class.java)

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(1))
            .processPageState(stateCaptor.capture()?:anyPageState())
        Mockito.verify(view, Mockito.times(1)).processEffect(effectCaptor.capture()?:anyEffect())

        assert(expectedEffect.person == expectedPerson)
        Assert.assertEquals(stateCaptor.value, PersonState.Loading)
        Assert.assertEquals(effectCaptor.value.person, PersonEffect.OnPersonSaved(Person(name)).person)
    }

    //FAIL

    @Test
    fun fetchPersonFail() {
        //given
        setupFail()
        Mockito.`when`(getPersonUseCase.execute()).thenReturn(anySingleThrowable())

        //when
        presenter.processIntent(PersonEvent.OnLoad)

        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)
        val effectCaptor  = ArgumentCaptor.forClass(PersonEffect::class.java)

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(1))
            .processPageState(stateCaptor.capture()?:anyPageState())
        Mockito.verify(view, Mockito.times(1)).processEffect(
            effectCaptor.capture()?: anyEffect()
        )

        Assert.assertEquals(stateCaptor.value, PersonState.Loading)
        Assert.assertEquals(effectCaptor.value, PersonEffect.OnFetchPersonFailed)
    }

    @Test
    fun onUpdatePersonFail() {
        //given
        setupFail()

        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)
        val effectCaptor  = ArgumentCaptor.forClass(PersonEffect::class.java)

        Mockito.`when`(updatePersonUseCase.execute(anyPerson())).thenReturn(anySingleThrowable())
        //when
        presenter.processIntent(PersonEvent.OnSubmitClicked(anyString()))

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(1))
            .processPageState(stateCaptor.capture()?:anyPageState())
        Mockito.verify(view, Mockito.times(1)).processEffect(
            effectCaptor.capture()?: PersonEffect.OnPersonSavedFailed
        )

        Assert.assertEquals(stateCaptor.value, PersonState.Loading)
        Assert.assertEquals(effectCaptor.value, PersonEffect.OnPersonSavedFailed)
    }

    //CANCEL

    @Test
    fun fetchPersonCancel(){
        setupCancel()

        //given
        Mockito.`when`(getPersonUseCase.execute()).thenReturn(
            anySinglePerson().delay(1, TimeUnit.SECONDS)
        )

        //when
        presenter.processIntent(PersonEvent.OnLoad)
        presenter.processIntent(PersonEvent.OnRelease)


        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(1))
            .processPageState(stateCaptor.capture()?:anyPageState())
        Mockito.verifyNoMoreInteractions(view)

        Assert.assertEquals(stateCaptor.value, PersonState.Loading)
    }

    @Test
    fun updatePersonCancel(){
        setupCancel()

        //given
        Mockito.`when`(updatePersonUseCase.execute(anyPerson())).thenReturn(
           anySinglePerson().delay(1, TimeUnit.SECONDS)
        )

        //when
        presenter.processIntent(PersonEvent.OnSubmitClicked(anyString()))
        presenter.processIntent(PersonEvent.OnRelease)

        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(1))
            .processPageState(stateCaptor.capture()?:anyPageState())
        Mockito.verifyNoMoreInteractions(view)

        Assert.assertEquals(stateCaptor.value, PersonState.Loading)
    }

    @Test
    fun cancel(){
        setupCancel()
        presenter.processIntent(PersonEvent.OnRelease)
    }


    //SETUP

    private fun setupSuccess(){
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        view = mock(PersonView::class.java)
        getPersonUseCase = mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = mock(UpdatePersonUseCaseContract::class.java)
        router = mock(MainRouterContract::class.java)
        interactor = MainInteractor(getPersonUseCase, updatePersonUseCase)
        presenter = PersonPresenter(view, router, interactor)
    }

    private fun setupFail(){
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        view = mock(PersonView::class.java)
        getPersonUseCase = mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = mock(UpdatePersonUseCaseContract::class.java)
        router = mock(MainRouterContract::class.java)
        interactor = MainInteractor(getPersonUseCase, updatePersonUseCase)
        presenter = PersonPresenter(view, router, interactor)
    }

    private fun setupCancel(){
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        view = mock(PersonView::class.java)
        getPersonUseCase = mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = mock(UpdatePersonUseCaseContract::class.java)
        router = mock(MainRouterContract::class.java)
        interactor = MainInteractor(getPersonUseCase, updatePersonUseCase)
        presenter = PersonPresenter(view, router, interactor)
    }

    private fun anyPageState() : PersonState = PersonState.Loading

    //UTILITIES

    private fun captureString(captor: ArgumentCaptor<String>): String = captor.capture() ?: ""

    private fun <T> capture(captor: ArgumentCaptor<T>, defaultValue : T): T = captor.capture() ?: defaultValue

    private fun anySingleThrowable() : Single<Person> = Single.error(anyThrowable())

    private fun anyThrowable() = Throwable()

    private fun anySinglePerson() = Single.just(anyPerson())

    private fun anyPerson() = Person(anyString())

    private fun anyString() = ""

    private fun anyEffect() = PersonEffect.OnFetchPersonFailed

}