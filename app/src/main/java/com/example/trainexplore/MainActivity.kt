package com.example.trainexplore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.trainexplore.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Comboios())

        binding.bottomNavigationView.setOnItemSelectedListener {

            when(it.itemId){

                R.id.comboios -> replaceFragment(Comboios())
                R.id.favoritos -> replaceFragment(Favoritos())
                R.id.noticias -> replaceFragment(Noticias())
                R.id.perfil -> replaceFragment(Perfil())

                else ->{

                }
            }
            true
        }

    }

    private fun replaceFragment(fragment : Fragment)
    {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
}