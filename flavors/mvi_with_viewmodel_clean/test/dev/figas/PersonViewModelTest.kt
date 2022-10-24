package dev.figas

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import dev.figas.domain.models.Person
import dev.figas.domain.usecases.GetPersonUseCaseContract
import dev.figas.domain.usecases.UpdatePersonUseCaseContract
import dev.figas.intent.event.PersonEvent
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
import dev.figas.viewmodel.PersonViewModel
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Test
import org.mockito.Mockito
import java.util.concurrent.TimeUnit

class PersonViewModelTest {

    lateinit var getPersonUseCase: GetPersonUseCaseContract
    lateinit var updatePersonUseCase: UpdatePersonUseCaseContract
    lateinit var viewModel: PersonViewModel

    //SUCCESS

    @Test
    fun fetchPersonSuccess(){
        //given
        setupSuccess()
        setupArch()

        assert(viewModel.uiState.test {
            Mockito.`when`(getPersonUseCase.execute()).thenReturn(Single.just(Person("hello")))

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
        val observer = viewModel.effect.test()

        Mockito.`when`(updatePersonUseCase.execute(anyPerson())).thenReturn(Single.just(Person("world")))

        viewModel.sendIntent(PersonEvent.OnSubmitClicked(anyString()))

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

        val observer = viewModel.effect.test()
        assert(viewModel.uiState.test {
            Mockito.`when`(getPersonUseCase.execute()).thenReturn(anySingleThrowable())

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

        val observer = viewModel.effect.test()
        assert(viewModel.uiState.test {
            Mockito.`when`(updatePersonUseCase.execute(anyPerson())).thenReturn(Single.error(Throwable()))

            //when
            viewModel.sendIntent(PersonEvent.OnSubmitClicked(anyString()))
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
            Mockito.`when`(getPersonUseCase.execute())
                .thenReturn(anySinglePerson().delay(1, TimeUnit.SECONDS))
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
            Mockito.`when`(updatePersonUseCase.execute(anyPerson()))
                .thenReturn(anySinglePerson().delay(1, TimeUnit.SECONDS))
            viewModel.sendIntent(PersonEvent.OnSubmitClicked(anyString()))
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

        getPersonUseCase = Mockito.mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = Mockito.mock(UpdatePersonUseCaseContract::class.java)
        viewModel = PersonViewModel(getPersonUseCase, updatePersonUseCase)
    }

    private fun setupFailure() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        setupArch()

        getPersonUseCase = Mockito.mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = Mockito.mock(UpdatePersonUseCaseContract::class.java)
        viewModel = PersonViewModel(getPersonUseCase, updatePersonUseCase)
    }

    private fun setupCancel(){
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        setupArch()

        getPersonUseCase = Mockito.mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = Mockito.mock(UpdatePersonUseCaseContract::class.java)
        viewModel = PersonViewModel(getPersonUseCase, updatePersonUseCase)
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

    private fun anySingleThrowable() : Single<Person> = Single.error(anyThrowable())

    private fun anyThrowable() = Throwable()

    private fun anySinglePerson() = Single.just(anyPerson())

    private fun anyPerson() = Person(anyString())

    private fun anyString() = ""

}