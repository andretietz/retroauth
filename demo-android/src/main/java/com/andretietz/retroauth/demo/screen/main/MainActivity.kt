package com.andretietz.retroauth.demo.screen.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.andretietz.retroauth.AndroidOwnerStorage
import com.andretietz.retroauth.demo.R
import com.andretietz.retroauth.demo.databinding.ActivityRepositoryListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private val viewModel: AndroidMainViewModel by viewModels()

  @Inject
  lateinit var ownerStorage: AndroidOwnerStorage

  private lateinit var binding: ActivityRepositoryListBinding

  private val switchAccount = registerForActivityResult(SwitchAccountContract()) { account ->
    account?.let { ownerStorage.switchActiveOwner(it.type, it) }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityRepositoryListBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val adapter = RepositoryAdapter()

    binding.repositoryList.layoutManager = LinearLayoutManager(this)
    binding.repositoryList.adapter = adapter

    binding.swipeToRefresh.setOnRefreshListener { viewModel.loadRepositories() }

    viewModel.state.flowWithLifecycle(this.lifecycle, Lifecycle.State.STARTED)
      .onEach {
        Timber.e("result")
        binding.swipeToRefresh.isRefreshing = false
        when (it) {
          is MainViewModel.ViewState.InitialState -> {
            adapter.update(emptyList())
            binding.repositoryList.visibility = View.GONE
            binding.textEmpty.visibility = View.VISIBLE
          }
          is MainViewModel.ViewState.RepositoryUpdate -> {
            binding.repositoryList.visibility = View.VISIBLE
            binding.textEmpty.visibility = View.GONE
            adapter.update(it.repos)
          }
          is MainViewModel.ViewState.Error -> showError(it.throwable)
          is MainViewModel.ViewState.LoginSuccess -> {
            binding.repositoryList.visibility = View.VISIBLE
            binding.textEmpty.visibility = View.GONE
            show("Login success!")
          }
          is MainViewModel.ViewState.LogoutSuccess -> {
            binding.repositoryList.visibility = View.GONE
            binding.textEmpty.visibility = View.VISIBLE
            show("Logout success!")
          }
        }
      }
      .launchIn(lifecycleScope)

  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater: MenuInflater = menuInflater
    inflater.inflate(R.menu.menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.menuitem_add_account -> {
        viewModel.addAccount()
        true
      }
      R.id.menuitem_invalidate_token -> {
        viewModel.invalidateTokens()
        true
      }
      R.id.menuitem_switch_accounts -> {
        switchAccount.launch(viewModel.getCurrentAccount())
        true
      }
      R.id.menuitem_logout -> {
        viewModel.logout()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun show(toShow: String) {
    Toast.makeText(applicationContext, toShow, Toast.LENGTH_SHORT).show()
  }

  private fun showError(error: Throwable) = show(error.toString())
}
