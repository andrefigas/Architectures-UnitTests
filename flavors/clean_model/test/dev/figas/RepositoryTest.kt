package dev.figas

import dev.figas.data.mappers.PersonMapper
import dev.figas.data.mappers.PersonMapperContract
import dev.figas.data.repositories.PersonRepository
import dev.figas.domain.models.Person
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class RepositoryTest {

    private lateinit var mapper: PersonMapperContract
    private lateinit var repo: PersonRepository

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        mapper = PersonMapper()
        repo = PersonRepository(mapper)
    }

    @Test
    fun fetchPerson(){
        //given
        val name = "hello"

        //when
        repo.providePerson().test()
            //then
            .assertValue { person ->
                person.name == "hello"
            }
    }

    @Test
    fun updatePerson(){
        //given
        val name = "world"

        //when
        repo.injectPerson(Person(name)).test()
            //then
            .assertValue { person ->
                person.name == name
            }
    }

}