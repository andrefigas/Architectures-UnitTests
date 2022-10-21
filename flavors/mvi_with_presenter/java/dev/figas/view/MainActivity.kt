package dev.figas.view

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.figas.R
import dev.figas.intent.event.PersonEvent
import dev.figas.intent.vieweffect.PersonEffect
import dev.figas.intent.viewstate.PersonState
import dev.figas.model.PersonModel
import dev.figas.presenter.PersonPresenter
import dev.figas.presenter.PersonPresenterContract

class MainActivity : AppCompatActivity(), PersonView {

    private lateinit var presenter: PersonPresenterContract
    private lateinit var nameEt: EditText
    private lateinit var progressPb: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = PersonPresenter(this, PersonModel())

        setContentView(R.layout.activity_main)
        setupUI()

        presenter.processIntent(PersonEvent.OnLoad)
    }

    private fun setupUI() {
        nameEt = findViewById(R.id.name_et)
        progressPb = findViewById(R.id.progress_pb)
        findViewById<View>(R.id.submit_bt).setOnClickListener {
            presenter.processIntent(PersonEvent.OnSubmitClicked(nameEt.text.toString()))
        }
    }

    override fun processEffect(personEffect: PersonEffect){
        when(personEffect){
            is PersonEffect.OnPersonSaved ->{
                hideLoading()
                showSavedPerson(personEffect.person.name)
            }
            PersonEffect.OnFetchPersonFailed -> {
                hideLoading()
                showPersonNameFailed()
            }
            PersonEffect.OnPersonSavedFailed -> {
                hideLoading()
                showSavePersonFailed()
            }
        }
    }

    override fun processPageState(personState: PersonState){
        when(personState){
            is PersonState.Data ->{
                hideLoading()
                showPersonName(personState.user.name)
            }

            is PersonState.Loading -> {
                showLoading()
            }
        }
    }

    private fun showPersonName(name: String) {
        nameEt.setText(name)
    }

    private fun showPersonNameFailed() {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
    }

    private fun showSavePersonFailed() {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
    }

    private fun showSavedPerson(name: String) {
        Toast.makeText(
            this,
            getString(R.string.submit_success_message, name),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showLoading() {
        progressPb.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressPb.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.processIntent(PersonEvent.OnRelease)
    }
}

interface PersonView {
    fun processEffect(personEffect: PersonEffect)
    fun processPageState(personState: PersonState)
}
