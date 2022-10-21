package dev.figas

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import dev.figas.intent.event.PersonEvent
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
import dev.figas.model.Person
import dev.figas.model.PersonModel
import dev.figas.model.PersonModelContract
import dev.figas.viewmodel.PersonViewModel
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Test
import org.mockito.Mockito
import java.util.concurrent.TimeUnit

class PersonViewModelTest {

    lateinit var model: PersonModelContract
    lateinit var viewModel: PersonViewModel

    //SUCCESS

    @Test
    fun fetchPersonSuccess(){
        //given
        setupSuccess()
        setupArch()

        assert(viewModel.uiState.test {
            //when
            viewModel.sendIntent(PersonEvent.OnLoad)
            //then
        }.toTypedArray().contentEquals(
            arrayOf(
                PersonState.Loading,
                PersonState.Data(Person("hello"))
            )
        ))

    }
    
    @Test
    fun updatePersonSuccess(){
        //given
        setupSuccess()
        setupArch()
        val observer = viewModel.effects.test()

        viewModel.sendIntent(PersonEvent.OnSubmitClicked("world"))

        observer.assertValue {  effect : PersonEffect ->
            //then
            effect == PersonEffect.OnPersonSaved(Person("world"))
        }

    }
    //FAILURE

    @Test
    fun fetchPersonFailure(){
        //given
        setupArch()
        setupFailure()

        val observer = viewModel.effects.test()
        assert(viewModel.uiState.test {
            Mockito.`when`(model.providePerson()).thenReturn(Single.error(Throwable()))

            //when
            viewModel.sendIntent(PersonEvent.OnLoad)
            //then
        }.toTypedArray().contentEquals(
            arrayOf(
                PersonState.Loading
            )
        ))

        observer.assertValue {  effect : PersonEffect ->
            effect == PersonEffect.OnFetchPersonFailed
        }

    }

    @Test
    fun updatePersonFailure(){
        //given
        setupArch()
        setupFailure()

        val observer = viewModel.effects.test()
        assert(viewModel.uiState.test {
            Mockito.`when`(model.injectPerson(Person(""))).thenReturn(Single.error(Throwable()))

            //when
            viewModel.sendIntent(PersonEvent.OnSubmitClicked(""))
            //then
        }.toTypedArray().contentEquals(
            arrayOf(
                PersonState.Loading
            )
        ))

        observer.assertValue {  effect : PersonEffect ->
            effect == PersonEffect.OnPersonSavedFailed
        }

    }

    //CANCEL

    @Test
    fun fetchPersonCancel(){
        //given
        setupCancel()
        setupArch()

        assert(viewModel.uiState.test {
            //when
            Mockito.`when`(model.providePerson())
                .thenReturn(Single.just(Person("")).delay(1, TimeUnit.SECONDS))
            viewModel.sendIntent(PersonEvent.OnLoad)
            viewModel.sendIntent(PersonEvent.OnRelease)
        }.toTypedArray().contentEquals(
            arrayOf(PersonState.Loading)
        ))

    }

    @Test
    fun updatePersonCancel(){
        //given
        setupCancel()
        setupArch()

        assert(viewModel.uiState.test {
            //when
            Mockito.`when`(model.injectPerson(Person("")))
                .thenReturn(Single.just(Person("")).delay(1, TimeUnit.SECONDS))
            viewModel.sendIntent(PersonEvent.OnSubmitClicked(""))
            viewModel.sendIntent(PersonEvent.OnRelease)
        }.toTypedArray().contentEquals(
            arrayOf(PersonState.Loading)
        ))

    }

    //SETUP

    private fun setupSuccess() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        setupArch()

        model = PersonModel()
        viewModel = PersonViewModel(model)
    }

    private fun setupFailure() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        setupArch()

        model = Mockito.mock(PersonModelContract::class.java)
        viewModel = PersonViewModel(model)
    }

    private fun setupCancel(){
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        setupArch()

        model = Mockito.mock(PersonModelContract::class.java)
        Mockito.`when`(model.injectPerson(Person(Mockito.anyString()))).thenReturn(Single.error(Throwable()))
        viewModel = PersonViewModel(model)
    }

    private fun setupArch(){
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) {
                runnable.run()
            }

            override fun postToMainThread(runnable: Runnable) {
                runnable.run()
            }

            override fun isMainThread(): Boolean {
                return true
            }
        })
    }

    private fun <T> LiveData<T>.test(trigger : ()-> Unit): MutableList<T?> {
        val list = mutableListOf<T?>()

        ArchTaskExecutor.getMainThreadExecutor().execute {

            val observer = Observer<T> { o -> list.add(o) }

            observeForever(observer)

            trigger()
            this@test.removeObserver(observer)
        }

        return list
    }

}