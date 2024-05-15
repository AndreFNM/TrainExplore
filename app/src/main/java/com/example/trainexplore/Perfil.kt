package com.example.trainexplore

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.trainexplore.R
import com.example.trainexplore.loginSystem.LoginActivity
import com.example.trainexplore.loginSystem.SessionManager
import com.example.trainexplore.loginSystem.UtilizadorPerfilViewModel
import com.example.trainexplore.loginSystem.UtilizadorPerfilViewModelFactory
import com.google.android.material.snackbar.Snackbar

class Perfil : Fragment() {
    private lateinit var logoutButton: Button
    private lateinit var saveChangesButton: Button
    private lateinit var editTextName: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var textViewEmail: TextView
    private lateinit var viewModel: UtilizadorPerfilViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = UtilizadorPerfilViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(UtilizadorPerfilViewModel::class.java)

        // Botões
        logoutButton = view.findViewById(R.id.logoutButton)
        saveChangesButton = view.findViewById(R.id.button_save_changes)

        // EditText para nome e meail
        editTextName = view.findViewById(R.id.edit_text_name)
        editTextPassword = view.findViewById(R.id.pass_atual)
        textViewEmail = view.findViewById(R.id.text_view_email)

        // EditText para mudança de passwords
        val editTextCurrentPassword = view.findViewById<EditText>(R.id.pass_atual)
        val editTextNewPassword = view.findViewById<EditText>(R.id.nova_pass)
        val editTextConfirmNewPassword = view.findViewById<EditText>(R.id.confirmar_nova_pass)

        val userId = SessionManager.getUserById(requireContext())
        if (userId == -1) {
            Toast.makeText(context, "Sessão Invalida, po favor faça login outra vez.", Toast.LENGTH_LONG).show()
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
        } else {
            viewModel.loadUserData(userId)
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                editTextName.setText(user.nome)
                textViewEmail.text = user.email
            } else {
                editTextName.setText("")
                textViewEmail.setText("No Email")
                Toast.makeText(context, "Nenhum dado de utilizador disponível. Por favor faça login outra vez.", Toast.LENGTH_LONG).show()
            }
        }

        saveChangesButton.setOnClickListener {
            val newName = editTextName.text.toString().trim()
            val currentPassword = editTextCurrentPassword.text.toString().trim()
            val newPassword = editTextNewPassword.text.toString().trim()
            val confirmNewPassword = editTextConfirmNewPassword.text.toString().trim()

            // verificar se está a tentar mudar a password
            if (currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmNewPassword.isNotBlank()) {
                if (newPassword == confirmNewPassword) {
                    viewModel.changePassword(userId, currentPassword, newPassword)
                } else {
                    Toast.makeText(context, "Novas passwords não são iguais", Toast.LENGTH_SHORT).show()
                }
            } else if (newName.isNotBlank()) {
                // dar update apenas ao nome se nenhuma password estiver escrita
                viewModel.updateUserData(userId, newName, null.toString())
            } else {
                Toast.makeText(context, "Por favor preencha os campos necessários", Toast.LENGTH_SHORT).show()
            }
        }


        viewModel.passwordUpdateResult.observe(viewLifecycleOwner) { result ->
            Snackbar.make(view, result, Snackbar.LENGTH_LONG).show()
        }

        viewModel.errorMessages.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }


        logoutButton.setOnClickListener {
            SessionManager.clearSession(requireContext())
            val intent = Intent(activity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

}
