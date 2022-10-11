package dev.figas

import dev.figas.data.mappers.PersonMapperContract
import dev.figas.domain.models.Person
import dev.figas.domain.repositories.PersonRepoContract
import dev.figas.domain.usecases.GetPersonUseCase
import dev.figas.domain.usecases.UpdatePersonUseCase

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class UseCasesTest {

    private lateinit var getPersonUseCase : GetPersonUseCase
    private lateinit var updatePersonUseCase: UpdatePersonUseCase
    private lateinit var mapper: PersonMapperContract
    private lateinit var repo: PersonRepoContract

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
    fun fetchPerson(){
        //given
        val name = "hello"
        `when`(repo.providePerson()).thenReturn(Single.just(Person(name)))

        //when
        getPersonUseCase.execute().test()
            //then
            .assertValue { person ->
            person.name == "hello"
        }
    }

    @Test
    fun updatePerson(){
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

}