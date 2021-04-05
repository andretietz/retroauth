package com.andretietz.retroauth.demo.screen.main

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.andretietz.retroauth.AndroidOwnerStorage
import com.andretietz.retroauth.demo.databinding.ActivityRepositoryListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  companion object {
    const val ACCOUNT_CHOOSER_REQUESTCODE = 0x123
  }

  private val viewModel: MainViewModel by viewModels()

  @Inject
  lateinit var ownerStorage: AndroidOwnerStorage

  private lateinit var binding: ActivityRepositoryListBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityRepositoryListBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val adapter = RepositoryAdapter()

    binding.repositoryList.layoutManager = LinearLayoutManager(this)
    binding.repositoryList.adapter = adapter

//    views.buttonAddAccount.setOnClickListener { viewModel.addAccount() }

    binding.swipeToRefresh.setOnRefreshListener {
      lifecycleScope.launch { viewModel.loadRepositories() }
    }


//    views.buttonInvalidateToken.setOnClickListener { viewModel.invalidateTokens() }

//    views.buttonSwitchAccount.setOnClickListener {
//      viewModel.createSwitchAccountIntent().also {
//        startActivityForResult(it, ACCOUNT_CHOOSER_REQUESTCODE)
//      }
//    }
//
//    views.buttonLogout.setOnClickListener { viewModel.logout() }

    viewModel.state.flowWithLifecycle(this.lifecycle, Lifecycle.State.STARTED)
      .onEach {
        binding.swipeToRefresh.isRefreshing = false
        when (it) {
          is MainViewModel.ViewState.InitialState -> {
          }
          is MainViewModel.ViewState.RepositoryUpdate -> adapter.update(it.repos)
          is MainViewModel.ViewState.Error -> showError(it.throwable)
          is MainViewModel.ViewState.LoginSuccess -> show("Login success!")
          is MainViewModel.ViewState.LogoutSuccess -> show("Logout success!")
        }
      }
      .launchIn(lifecycleScope)

  }


  private suspend fun show(toShow: String) = withContext(Dispatchers.Main) {
    Toast.makeText(applicationContext, toShow, Toast.LENGTH_SHORT).show()
  }

  private suspend fun showError(error: Throwable) = show(error.toString())

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == ACCOUNT_CHOOSER_REQUESTCODE && resultCode == RESULT_OK) {
      lifecycleScope.launch {
        if (data != null) {
          val type = requireNotNull(data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE))
          val name = requireNotNull(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME))
          ownerStorage.switchActiveOwner(type, Account(name, type))
          show("Account switched to $name")
        } else {
          show("Wasn't able to switch accounts")
        }
      }
    }
  }
}
