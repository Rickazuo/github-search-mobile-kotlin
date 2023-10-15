package br.com.igorbag.githubsearch.ui.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(private val context: Context, private val repositories: List<Repository>) : RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    // Cria uma nova view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    // Pega o conteúdo da view e troca pela informação de item de uma lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val repository = repositories[position]

        holder.cardRepositorio.setOnClickListener {
            openBrowser(context, repository.htmlUrl)
        }

        holder.nomeRepositorio.text = repository.name

        holder.shareIconButton.setOnClickListener {
            shareRepositoryLink(context, repository.htmlUrl)
        }
    }

    // Pega a quantidade de repositórios da lista
    override fun getItemCount(): Int = repositories.size

    // Conecta elementos com o layout
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val cardRepositorio : CardView
        val nomeRepositorio : TextView
        val shareIconButton : ImageView

        init {
            view.apply {
                cardRepositorio = findViewById(R.id.cv_repository)
                nomeRepositorio = findViewById(R.id.tv_repository_name)
                shareIconButton = findViewById(R.id.iv_share)
            }
        }

    }

    // Método responsável por compartilhar o link do repositório selecionado
    fun shareRepositoryLink(context: Context, urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    // Método responsável por abrir o browser com o link informado do repositório
    fun openBrowser(context: Context, urlRepository: String) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }

}