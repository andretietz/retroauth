package com.andretietz.retroauth.demo.screen.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.andretietz.retroauth.demo.R
import com.andretietz.retroauth.demo.api.GithubApi
import com.andretietz.retroauth.demo.databinding.ListitemRepositoryBinding

class RepositoryAdapter : RecyclerView.Adapter<RepositoryAdapter.RepositoryViewHolder>() {


  private val repositories: MutableList<GithubApi.Repository> = mutableListOf()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RepositoryViewHolder(
    ListitemRepositoryBinding
      .bind(
        LayoutInflater.from(parent.context)
          .inflate(R.layout.listitem_repository, parent, false)
      )
  )

  override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) =
    holder.bind(repositories[position])

  override fun getItemCount() = repositories.size

  fun update(items: List<GithubApi.Repository>) {
    repositories.clear()
    repositories.addAll(items)
    DiffUtil
      .calculateDiff(RepositoryDiff(repositories, items), true)
      .dispatchUpdatesTo(this)
    notifyDataSetChanged()
  }

  class RepositoryViewHolder(private val binding: ListitemRepositoryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(repository: GithubApi.Repository) {
      binding.textRepositoryName.text = repository.name
      binding.imagePrivate.setImageResource(
        if (repository.private) R.drawable.ic_baseline_lock_24
        else R.drawable.ic_baseline_lock_open_24
      )
    }
  }

  class RepositoryDiff(
    private val old: List<GithubApi.Repository>,
    private val new: List<GithubApi.Repository>
  ) : DiffUtil.Callback() {

    override fun getOldListSize() = old.size

    override fun getNewListSize() = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
      old[oldItemPosition].id == new[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
      old[oldItemPosition] == new[newItemPosition]
  }
}
