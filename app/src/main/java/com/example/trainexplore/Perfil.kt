package com.example.trainexplore

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.trainexplore.loginSystem.LoginActivity


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Perfil : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var logoutButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logoutButton = view.findViewById(R.id.logoutButton)
        sharedPreferences = requireActivity().getSharedPreferences("MeuPerfil", AppCompatActivity.MODE_PRIVATE)

        logoutButton.setOnClickListener {
            //apaga os dados da sharedPreferences de um utilizador e faz o logout
            sharedPreferences.edit().putBoolean("IsUtilizadorLoggedIn",false).apply()

            //redirecional para LoginActivity
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            //Como é um fragmento, esta função fecha a atividade principal
            requireActivity().finish()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Perfil().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}