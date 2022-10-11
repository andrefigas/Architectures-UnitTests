package dev.figas

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.figas.model.Person
import dev.figas.model.PersonModel
import dev.figas.model.PersonModelContract
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentCaptor.forClass
import org.mockito.Mockito.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun test(){
        val bar : Bar = mock(Bar::class.java)
        bar.doSomething(Person("John"))

        val argument: ArgumentCaptor<Person> = forClass(Person::class.java)
        verify(bar, times(1)).doSomething(argument.capture())
        bar.doSomething(Person("John"))

        assertEquals("John", argument.getValue().name)
    }


}

open class Bar{
    fun doSomething(value : Person?){
        println("append $value")
    }
}