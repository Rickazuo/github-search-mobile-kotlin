package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
//import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
//import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var userName: EditText
    lateinit var searchButton: Button
    lateinit var listOfRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var loading : ProgressBar
    lateinit var icWifiOff : ImageView
    lateinit var txtWifiOff : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayShowHomeEnabled(true)

        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        setupListeners()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        userName = findViewById(R.id.et_user_name)
        searchButton = findViewById(R.id.search_button)
        listOfRepositories = findViewById(R.id.rv_list_of_repositories)
        loading = findViewById(R.id.loading)
        icWifiOff = findViewById(R.id.iv_wifi_off)
        txtWifiOff = findViewById(R.id.tv_wifi_off)
    }

    //Método responsável por fazer a configuração base do Retrofit
    fun setupRetrofit() {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        searchButton.setOnClickListener {

            val connection = isInternetAvailable()

            if (!connection) {
                icWifiOff.isVisible = true
                txtWifiOff.isVisible = true
            } else {

                icWifiOff.isVisible = false
                txtWifiOff.isVisible = false

                val searchName = userName.text.toString()
                getAllReposByUserName(searchName)
                saveUserLocal()
                listOfRepositories.isVisible = false //Para que desapareça quando for fazer nossas pesquisas
            }
        }
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }


    // Método responsável por buscar todos os repositórios do usuário fornecido
    fun getAllReposByUserName(userName: String) {

        if (userName.isNotEmpty()) {

            loading.isVisible = true

            githubApi.getAllRepositoriesByUser(userName)
                .enqueue(object : Callback<List<Repository>> {

                    override fun onResponse(
                        call: Call<List<Repository>>,
                        response: Response<List<Repository>>
                    ) {
                        if (response.isSuccessful) {

                            loading.isVisible = false
                            listOfRepositories.isVisible = true

                            val repositories = response.body()

                            repositories?.let {
                                setupAdapter(repositories)
                            }

                        } else {

                            loading.isVisible = false

                            val context = applicationContext
                            Toast.makeText(context, R.string.response_error, Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                    override fun onFailure(call: Call<List<Repository>>, t: Throwable) {

                        loading.isVisible = false

                        val context = applicationContext
                        Toast.makeText(context, R.string.response_error, Toast.LENGTH_LONG).show()
                    }

                })
        }
    }

    // Método responsável por realizar a configuração do adapter
    fun setupAdapter(list: List<Repository>) {

        val adapter = RepositoryAdapter(
            this, list)

        listOfRepositories.adapter = adapter
    }

    // Método responsável por salvar o usuário preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {

        val searchResult = userName.text.toString()

        val sharedPreference = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPreference.edit()) {
            putString("saved_username", searchResult)
            apply()
        }
    }

    //Método que exibe o que foi salvo na SharedPreference
    private fun showUserName() {

        val sharedPreference = getPreferences(MODE_PRIVATE) ?: return
        val lastSearch = sharedPreference.getString("saved_username", null)

        if (!lastSearch.isNullOrEmpty()) {
            userName.setText(lastSearch)
        }
    }

}