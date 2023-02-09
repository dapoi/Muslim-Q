package com.prodev.muslimq.presentation.view.others

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.BuildConfig
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.OthersObject
import com.prodev.muslimq.databinding.FragmentOthersBinding
import com.prodev.muslimq.presentation.adapter.OthersAdapter

class OthersFragment : Fragment() {

    private val binding: FragmentOthersBinding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentOthersBinding.inflate(layoutInflater)
    }

    private lateinit var othersAdapter: OthersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()
        binding.tvVersion.text = "Versi ${BuildConfig.VERSION_NAME}"
    }

    private fun setRecyclerView() {
        othersAdapter = OthersAdapter()
        binding.rvOthers.apply {
            adapter = othersAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
        othersAdapter.setList(OthersObject.listData)
        othersAdapter.onClick = {
            when (it) {
                0 -> {
                    findNavController().navigate(
                        R.id.action_othersFragment_to_quranBookmarkFragment
                    )
                }
                1 -> {
                    val email = "luthfidaffa2202@gmail.com"
                    val subject = "Feedback Aplikasi MuslimQ"
                    val body = "Silahkan tulis pesan Anda di sini"

                    // intent to send email
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    startActivity(intent)
                }
                2 -> {
                    Toast.makeText(context, "Segera Datang", Toast.LENGTH_SHORT).show()
                }
                3 -> {
                    Toast.makeText(context, "Tentang", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}