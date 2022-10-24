package dev.figas

import dev.figas.data.mappers.PersonMapperContract
import dev.figas.domain.models.Person
import dev.figas.domain.repositories.PersonRepoContract
import dev.figas.domain.usecases.GetPersonUseCase
import dev.figas.domain.usecases.GetPersonUseCaseContract
import dev.figas.domain.usecases.UpdatePersonUseCase
import dev.figas.domain.usecases.UpdatePersonUseCaseContract

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class UseCasesTest {

    private lateinit var mapper: PersonMapperContract
    private lateinit var repo: PersonRepoContract
    private lateinit var getPersonUseCase : GetPersonUseCaseContract
    private lateinit var updatePersonUseCase: UpdatePersonUseCaseContract

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        mapper = mock(PersonMapperContract::class.java)
        repo = mock(PersonRepoContract::class.java)
        getPersonUseCase = GetPersonUseCase(repo)
        updatePersonUseCase = UpdatePersonUseCase(repo)
    }

    @Test
    fun fetchPersonSuccess(){
        //given
        `when`(repo.providePerson()).thenReturn(Single.just(Person("hello")))

        //when
        getPersonUseCase.execute().test()
            //then
            .assertValue { person ->
            person.name == "hello"
        }
    }

    @Test
    fun fetchPersonFailure(){
        //given
        val error = Throwable()
        `when`(repo.providePerson()).thenReturn(Single.error(error))

        //when
        repo.providePerson().test()
            //then
            .assertError(error)
    }

    @Test
    fun updatePersonSuccess(){
        //given
        val name = "world"
        `when`(repo.injectPerson(Person(name))).thenReturn(Single.just(Person(name)))

        //when
        updatePersonUseCase.execute(Person(name)).test()
            //then
            .assertValue { person ->
            person.name == name
        }
    }

    @Test
    fun updatePersonFailure(){
        //given
        val name = "world"
        val error = Throwable()
        `when`(repo.injectPerson(Person(name))).thenReturn(Single.error(error))

        //when
        updatePersonUseCase.execute(Person(name)).test()
            //then
            .assertError(error)
    }

}