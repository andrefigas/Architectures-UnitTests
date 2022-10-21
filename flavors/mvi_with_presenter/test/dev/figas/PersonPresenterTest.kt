package dev.figas

import dev.figas.intent.event.PersonEvent
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
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
import org.mockito.Mockito
import java.util.concurrent.TimeUnit

class PersonPresenterTest {


    private lateinit var view : PersonView
    private lateinit var presenter : PersonPresenter
    private lateinit var model : PersonModelContract

    //SUCCESS

    @Test
    fun onFetchPersonSuccess(){
        //given
        setupSuccess()

        //when
        presenter.processIntent(PersonEvent.OnLoad)

        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(2))
            .processPageState(stateCaptor.capture()?: anyState())

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

        //when
        presenter.processIntent(PersonEvent.OnSubmitClicked(name))

        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)
        val effectCaptor : ArgumentCaptor<PersonEffect.OnPersonSaved>  = ArgumentCaptor.forClass(PersonEffect.OnPersonSaved::class.java)

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(1))
            .processPageState(stateCaptor.capture()?:anyState())
        Mockito.verify(view, Mockito.times(1)).processEffect(effectCaptor.capture()?:
        PersonEffect.OnPersonSaved(
            Person(name)
        ))

        assert(expectedEffect.person == expectedPerson)
        Assert.assertEquals(stateCaptor.value, PersonState.Loading)
        Assert.assertEquals(effectCaptor.value.person, PersonEffect.OnPersonSaved(Person(name)).person)
    }

    //FAIL

    @Test
    fun fetchPersonFail() {
        //given
        setupFail()
        Mockito.`when`(model.providePerson()).thenReturn(Single.error(Throwable()))

        //when
        presenter.processIntent(PersonEvent.OnLoad)

        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)
        val effectCaptor  = ArgumentCaptor.forClass(PersonEffect::class.java)

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(1))
            .processPageState(stateCaptor.capture()?:anyState())
        Mockito.verify(view, Mockito.times(1)).processEffect(
            effectCaptor.capture()?: PersonEffect.OnFetchPersonFailed
        )

        Assert.assertEquals(stateCaptor.value, PersonState.Loading)
        Assert.assertEquals(effectCaptor.value, PersonEffect.OnFetchPersonFailed)
    }

    @Test
    fun onUpdatePersonFail() {
        //given
        setupFail()
        val name = "world"

        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)
        val effectCaptor  = ArgumentCaptor.forClass(PersonEffect::class.java)

        Mockito.`when`(model.injectPerson(Person(name))).thenReturn(Single.error(Throwable()))
        //when
        presenter.processIntent(PersonEvent.OnSubmitClicked(name))

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(1))
            .processPageState(stateCaptor.capture()?:anyState())
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
        Mockito.`when`(model.providePerson()).thenReturn(
            Single.just(Person("")).delay(1, TimeUnit.SECONDS)
        )

        //when
        presenter.processIntent(PersonEvent.OnLoad)
        presenter.processIntent(PersonEvent.OnRelease)


        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(1))
            .processPageState(stateCaptor.capture()?:anyState())
        Mockito.verifyNoMoreInteractions(view)

        Assert.assertEquals(stateCaptor.value, PersonState.Loading)
    }

    private fun anyState() : PersonState = PersonState.Loading

    @Test
    fun updatePersonCancel(){
        setupCancel()

        //given
        Mockito.`when`(model.injectPerson(Person(""))).thenReturn(
            Single.just(Person("")).delay(1, TimeUnit.SECONDS)
        )

        //when
        presenter.processIntent(PersonEvent.OnSubmitClicked(""))
        presenter.processIntent(PersonEvent.OnRelease)

        //then
        val stateCaptor  = ArgumentCaptor.forClass(PersonState::class.java)

        Mockito.inOrder(view)
        Mockito.verify(view, Mockito.times(1))
            .processPageState(stateCaptor.capture()?:anyState())
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

        view = Mockito.mock(PersonView::class.java)
        model = PersonModel()
        presenter = PersonPresenter(view, model)
    }

    private fun setupFail(){
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        view = Mockito.mock(PersonView::class.java)
        model = Mockito.mock(PersonModelContract::class.java)
        presenter = PersonPresenter(view, model)
    }

    private fun setupCancel(){
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        view = Mockito.mock(PersonView::class.java)
        model = Mockito.mock(PersonModelContract::class.java)
        presenter = PersonPresenter(view, model)
    }

}