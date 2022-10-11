package dev.figas

import dev.figas.data.mappers.PersonMapper
import dev.figas.data.models.PersonDataModel

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class PersonMapperTest {

    private lateinit var mapper: PersonMapper

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        mapper = PersonMapper()
    }

    @Test
    fun map(){
        //given
        val name = "hello"
        val data = PersonDataModel(name)

        //when
        val person = mapper.toPerson(data)

        //then
        assert(name == person.name)
    }

}