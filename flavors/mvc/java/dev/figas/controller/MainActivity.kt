package dev.figas.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import dev.figas.R
import dev.figas.model.Person
import dev.figas.model.PersonModel
import dev.figas.model.PersonModelContract
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var model: PersonModelContract
    private lateinit var nameEt: EditText
    private lateinit var progressPb: ProgressBar

    private val requests = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = PersonModel()

        setContentView(R.layout.activity_main)
        setupUI()
        fetchPerson()
    }

    private fun setupUI() {
        nameEt = findViewById(R.id.name_et)
        progressPb = findViewById(R.id.progress_pb)
        findViewById<View>(R.id.submit_bt).setOnClickListener {
            injectPerson()
        }
    }

    private fun injectPerson() {
        showLoading()
        requests.add(
            model.injectPerson(Person(nameEt.text.toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { person ->
                        hideLoading()
                        onSavePerson(person)
                    },
                    { throwlable ->
                        hideLoading()
                        onSavePersonError()
                    }
                )
        )

    }

    private fun fetchPerson() {
        requests.add(
            model.providePerson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { person ->
                        hideLoading()
                        onReceivePerson(person)
                    },
                    { throwlable ->
                        hideLoading()
                        onReceivePersonError()
                    }
                )
        )
    }

    private fun onReceivePerson(person: Person) {
        nameEt.setText(person.name)
    }

    private fun onSavePerson(person: Person) {
        Toast.makeText(
            this,
            getString(R.string.submit_success_message, person.name),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showLoading() {
        progressPb.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressPb.visibility = View.INVISIBLE
    }

    private fun onReceivePersonError() {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        requests.dispose()
    }

    private fun onSavePersonError() {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
    }


}
