package dev.figas

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import dev.figas.model.Person
import dev.figas.model.PersonModel
import dev.figas.model.PersonModelContract
import dev.figas.viewmodel.PersonViewModel
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Test
import org.mockito.Mockito.*
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
        val data = viewModel.data.test {
            //when
            viewModel.fetchPerson()
        }

        //then
        assert(data.size == 1 && data[0]?.name == "hello")

    }

    @Test
    fun updatePersonSuccess(){
        //given
        setupSuccess()
        val observer = viewModel.insert.test()

        //when
        viewModel.injectPerson("world")

        observer.assertValue {  name : String ->
            //then
            name == "world"
        }

    }

    //FAIL

    @Test
    fun fetchPersonFailure(){
        //given
        setupFailure()

        val data = viewModel.data.test {
            //when
            `when`(model.providePerson()).thenReturn(Single.error(Throwable()))
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
            `when`(model.injectPerson(Person(""))).thenReturn(Single.error(Throwable()))
            //when
            viewModel.injectPerson("")
        }

        observer.assertValue {  name : String ->
            //then
            name.isEmpty()
        }
    }

    //Cancel

    @Test
    fun updatePersonCancel(){
        //given
        setupCancel()

        val observer = viewModel.insert.test()
        ArchTaskExecutor.getMainThreadExecutor().execute {
            `when`(model.injectPerson(Person(""))).thenReturn(Single.just(Person("")).delay(1, TimeUnit.SECONDS))
            //when
            viewModel.injectPerson("")
        }

        observer.assertEmpty()
    }

    @Test
    fun fetchPersonCancel(){
        //given
        setupCancel()

        val data = viewModel.data.test {
            //when
            `when`(model.providePerson()).thenReturn(Single.just(Person("")).delay(1, TimeUnit.SECONDS))
            viewModel.fetchPerson()
            viewModel.release()
        }

        //then
        assert(data.isEmpty())
    }

    private fun setupCancel(){
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        setupArch()

        model = mock(PersonModelContract::class.java)
        `when`(model.injectPerson(Person(anyString()))).thenReturn(Single.error(Throwable()))
        viewModel = PersonViewModel(model)
    }

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

        model = mock(PersonModelContract::class.java)
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