package dev.figas.view

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import dev.figas.R
import dev.figas.model.Person
import dev.figas.model.PersonModel
import dev.figas.viewmodel.PersonViewModel
import dev.figas.viewmodel.PersonViewModelFactory

class MainActivity : AppCompatActivity(){

    private lateinit var viewModel: PersonViewModel
    private lateinit var nameEt: EditText
    private lateinit var progressPb: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(
            this, PersonViewModelFactory(
                PersonModel()
            )
        ).get(PersonViewModel::class.java)

        setContentView(R.layout.activity_main)
        setupUI()

        viewModel.data.observe(this) { person : Person? ->
            hideLoading()
            if(person == null){
                showPersonNameError()
            }else{
                showPersonName(person.name)
            }

        }

        viewModel.insert.subscribe { name : String->
            hideLoading()
            if(name.isEmpty()){
                showSavedPersonError()
            }else{
                showSavedPerson(name)
            }
        }

        showLoading()
        viewModel.fetchPerson()
    }

    private fun setupUI() {
        nameEt = findViewById(R.id.name_et)
        progressPb = findViewById(R.id.progress_pb)
        findViewById<View>(R.id.submit_bt).setOnClickListener {
            showLoading()
            viewModel.injectPerson(nameEt.text.toString())
        }
    }

    private fun showPersonName(name: String) {
        nameEt.setText(name)
    }

    private fun showPersonNameError() {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
    }

    private fun showSavedPerson(name: String) {
        Toast.makeText(
            this,
            getString(R.string.submit_success_message, name),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showSavedPersonError() {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
    }


    private fun showLoading() {
        progressPb.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressPb.visibility = View.INVISIBLE
    }

}

