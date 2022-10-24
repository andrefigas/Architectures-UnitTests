package dev.figas

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import dev.figas.domain.models.Person
import dev.figas.domain.usecases.GetPersonUseCaseContract
import dev.figas.domain.usecases.UpdatePersonUseCaseContract

import dev.figas.viewmodel.PersonViewModel
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Test
import org.mockito.Mockito.*
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
        val data = viewModel.data.test {
            //when
            `when`(getPersonUseCase.execute()).thenReturn(Single.just(Person("hello")))
            viewModel.fetchPerson()
        }

        //then
        assert(data.size == 1 && data[0]?.name == "hello")

    }

    @Test
    fun updatePersonSuccess(){
        //given
        setupSuccess()
        val expected = Person("world")
        val observer = viewModel.insert.test()

        ArchTaskExecutor.getMainThreadExecutor().execute {
            `when`(updatePersonUseCase.execute(anyPerson())).thenReturn(Single.just(expected))
            //when
            viewModel.injectPerson(anyString())
        }

        observer.assertValue {  name : String ->
            //then
            name == expected.name
        }

    }

    //FAIL

    @Test
    fun fetchPersonFailure(){
        //given
        setupFailure()

        val data = viewModel.data.test {
            //when
            `when`(getPersonUseCase.execute()).thenReturn(anySingleThrowable())
            viewModel.fetchPerson()
        }

        //then
        assert(data[0] == null)
    }

    @Test
    fun updatePersonFailure(){
        //given
        setupFailure()

        val observer = viewModel.insert.test()
        ArchTaskExecutor.getMainThreadExecutor().execute {
            `when`(updatePersonUseCase.execute(anyPerson())).thenReturn(anySingleThrowable())
            //when
            viewModel.injectPerson(anyString())
        }

        observer.assertValue {  name : String ->
            //then
            name.isEmpty()
        }
    }

    //CANCEL

    @Test
    fun updatePersonCancel(){
        //given
        setupCancel()

        val observer = viewModel.insert.test()
        ArchTaskExecutor.getMainThreadExecutor().execute {
            `when`(updatePersonUseCase.execute(anyPerson())).thenReturn(anySinglePerson().delay(1, TimeUnit.SECONDS))
            //when
            viewModel.injectPerson(anyString())
        }

        observer.assertEmpty()
    }

    @Test
    fun fetchPersonCancel(){
        //given
        setupCancel()

        val data = viewModel.data.test {
            //when
            `when`(getPersonUseCase.execute()).thenReturn(anySinglePerson().delay(1, TimeUnit.SECONDS))
            viewModel.fetchPerson()
            viewModel.release()
        }

        //then
        assert(data.isEmpty())
    }

    //SETUP

    private fun setupCancel(){
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        setupArch()

        getPersonUseCase = mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = mock(UpdatePersonUseCaseContract::class.java)
        viewModel = PersonViewModel(getPersonUseCase, updatePersonUseCase)
    }

    private fun setupSuccess() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        setupArch()

        getPersonUseCase = mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = mock(UpdatePersonUseCaseContract::class.java)
        viewModel = PersonViewModel(getPersonUseCase, updatePersonUseCase)
    }

    private fun setupFailure() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        setupArch()

        getPersonUseCase = mock(GetPersonUseCaseContract::class.java)
        updatePersonUseCase = mock(UpdatePersonUseCaseContract::class.java)
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

    //UTILITIES

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