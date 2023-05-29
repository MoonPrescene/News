package com.example.news.ui.fragment

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.funiture_shop.data.model.res.Res
import com.example.funiture_shop.helper.SharedPreferencesHelper
import com.example.news.MainActivity
import com.example.news.R
import com.example.news.common.afterTextChanged
import com.example.news.common.showToast
import com.example.news.databinding.FragmentSignInBinding
import com.example.news.ui.viewmodel.SignInViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSignInBinding
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private val viewModel: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_in, container, false)
        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        //val sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        viewModel.apply {
            loginFormState.observe(viewLifecycleOwner) {
                val loginState = it

                // disable login button unless both username / password is valid
                login.isEnabled = loginState.isDataValid

                if (loginState.usernameError != null) {
                    username.error = getString(loginState.usernameError)
                }
                if (loginState.passwordError != null) {
                    password.error = getString(loginState.passwordError)
                }
            }

            signInRes.observe(viewLifecycleOwner) {
                loading.visibility = View.GONE
                when (it) {
                    is Res.Success<*> -> {
                        sharedPreferencesHelper.saveUserInfo(userName = username.text.toString())
                        val intent = Intent(
                            requireActivity(), MainActivity::class.java
                        )
                        startActivity(intent)
                    }

                    is Res.Error -> {
                        requireContext().showToast(
                            it.message
                        )
                    }
                }
            }
        }

        username.afterTextChanged {
            viewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                viewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        viewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                viewModel.login(username.text.toString(), password.text.toString())
            }
        }
        binding.register.setOnClickListener {
            findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment())
        }
        return binding.root
    }

}