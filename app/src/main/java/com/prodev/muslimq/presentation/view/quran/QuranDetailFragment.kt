package com.prodev.muslimq.presentation.view.quran

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.Ayat
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.isOnline
import com.prodev.muslimq.databinding.FragmentQuranDetailBinding
import com.prodev.muslimq.presentation.BaseActivity
import com.prodev.muslimq.presentation.adapter.QuranDetailAdapter
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class QuranDetailFragment : Fragment() {

    private lateinit var detailAdapter: QuranDetailAdapter
    private lateinit var sbCurrent: SeekBar
    private lateinit var mediaPlayer: MediaPlayer

    private var fontSize: Int? = null

    private var _binding: FragmentQuranDetailBinding? = null
    private val binding get() = _binding!!

    private val detailViewModel: QuranViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    private var audioIsPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuranDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()
        setViewModel()

        val surahName = arguments?.getString(SURAH_NAME)

        binding.apply {
            toolbar.title = surahName
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            ivDescSurah.setOnClickListener {
                showDescSurah()
            }

            ivFontSetting.setOnClickListener {
                showFontSettingDialog()
            }
        }
    }

    private fun setAdapter() {
        detailAdapter = QuranDetailAdapter()
        binding.rvAyah.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detailAdapter
            setHasFixedSize(true)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setViewModel() {
        val id = arguments?.getInt(SURAH_NUMBER)
        id?.let { idSurah ->
            detailViewModel.getQuranDetail(idSurah).observe(viewLifecycleOwner) { result ->
                with(binding) {
                    srlSurah.apply {
                        setLottieAnimation("loading.json")
                        setRepeatMode(SSPullToRefreshLayout.RepeatMode.REPEAT)
                        setRepeatCount(SSPullToRefreshLayout.RepeatCount.INFINITE)
                        setOnRefreshListener(object : SSPullToRefreshLayout.OnRefreshListener {
                            override fun onRefresh() {
                                val handlerData = Handler(Looper.getMainLooper())
                                val online = isOnline(requireContext())
                                if (online) {
                                    handlerData.postDelayed({
                                        setRefreshing(false)
                                    }, 2000)

                                    handlerData.postDelayed({
                                        if (result.data == null) {
                                            setViewModel()
                                            clNoInternet.visibility = View.GONE
                                        } else {
                                            clSurah.visibility = View.VISIBLE
                                            clSound.visibility = View.VISIBLE
                                            rvAyah.visibility = View.VISIBLE
                                            clNoInternet.visibility = View.GONE
                                            setUpMediaPlayer(result.data?.audio!!)
                                        }
                                    }, 2350)
                                } else {
                                    clSurah.visibility = View.GONE
                                    clSound.visibility = View.GONE
                                    rvAyah.visibility = View.GONE
                                    clNoInternet.visibility = View.VISIBLE
                                    setRefreshing(false)
                                }
                            }
                        })
                    }

                    when {
                        result is Resource.Loading && result.data == null -> {
                            stateLoading(true)
                        }
                        result is Resource.Error && result.data == null -> {
                            stateLoading(false)
                            clNoInternet.visibility = View.VISIBLE
                            clSurah.visibility = View.GONE
                            clSound.visibility = View.GONE
                        }
                        else -> {
                            stateLoading(false)

                            clNoInternet.visibility = View.GONE
                            tvSurahName.text = result.data?.namaLatin
                            tvAyahMeaning.text = result.data?.artiQuran
                            tvCityAndTotalAyah.text = "${
                                result.data?.tempatTurun?.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.getDefault()
                                    ) else it.toString()
                                }
                            } • ${result.data?.jumlahAyat} ayat"

                            val ayahs = ArrayList<Ayat>()
                            result.data?.ayat?.let { ayahs.addAll(it) }
                            if (ayahs[0].ayatTerjemahan.contains("Dengan nama Allah Yang Maha Pengasih, Maha Penyayang")) {
                                ayahs.removeAt(0)
                                detailAdapter.setList(ayahs)
                            } else {
                                detailAdapter.setList(ayahs)
                            }
                            rvAyah.visibility = View.VISIBLE

                            result.data?.let { quranDetailEntity ->
                                var bookmarked = quranDetailEntity.isBookmarked
                                setBookmark(bookmarked)
                                ivBookmark.setOnClickListener {
                                    bookmarked = !bookmarked
                                    setBookmark(bookmarked)
                                    detailViewModel.insertToBookmark(quranDetailEntity, bookmarked)
                                    if (bookmarked) {
                                        (activity as BaseActivity).customSnackbar(
                                            state = true,
                                            context = requireContext(),
                                            view = binding.root,
                                            message = "Berhasil ditambahkan ke \"Baca Nanti\"",
                                            action = true,
                                            toBookmark = true
                                        )
                                    } else {
                                        (activity as BaseActivity).customSnackbar(
                                            state = false,
                                            context = requireContext(),
                                            view = binding.root,
                                            message = "Berhasil dihapus dari \"Baca Nanti\"",
                                        )
                                    }
                                }

                                if (isOnline(requireContext())) {
                                    clSound.visibility = View.VISIBLE
                                    setUpMediaPlayer(quranDetailEntity.audio)
                                } else {
                                    clSound.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setUpMediaPlayer(audio: String) {
        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(audio)
            prepareAsync()
            with(binding) {
                setOnPreparedListener {
                    sbSound.max = mediaPlayer.duration

                    ivSound.setOnClickListener {
                        if (isPlaying) {
                            audioIsPlaying = false
                            pause()
                            ivSound.setImageResource(R.drawable.ic_play)
                        } else {
                            audioIsPlaying = true
                            start()
                            ivSound.setImageResource(R.drawable.ic_pause)
                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed(object : Runnable {
                                override fun run() {
                                    try {
                                        sbSound.progress = mediaPlayer.currentPosition
                                        handler.postDelayed(this, 1000)
                                    } catch (e: Exception) {
                                        sbSound.progress = 0
                                    }
                                }
                            }, 0)
                        }
                    }

                    var currentProgress = currentPosition
                    val audioDuration = duration
                    durationText(tvSoundDuration, currentProgress, audioDuration)

                    sbSound.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                seekTo(progress)
                            }

                            currentProgress = progress
                            durationText(tvSoundDuration, currentProgress, audioDuration)
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        }
                    })
                }

                setOnCompletionListener {
                    ivSound.setImageResource(R.drawable.ic_play)
                }
            }
        }
    }

    private fun durationText(tvDuration: TextView, progress: Int, duration: Int) {
        tvDuration.text = buildString {
            append(formatDuration(progress))
            append(" / ")
            append(formatDuration(duration))
        }
    }

    private fun formatDuration(duration: Int): String {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        return "${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
    }

    private fun setBookmark(bookmarkStatus: Boolean) {
        binding.apply {
            if (bookmarkStatus) {
                ivBookmark.setImageResource(R.drawable.ic_bookmark_true)
            } else {
                ivBookmark.setImageResource(R.drawable.ic_bookmark_false)
            }
        }
    }

    private fun stateLoading(state: Boolean) {
        binding.apply {
            if (state) {
                progressBar.visibility = View.VISIBLE
                progressHeader.visibility = View.VISIBLE
                clSurah.visibility = View.GONE
                clSound.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                progressHeader.visibility = View.GONE
                clSurah.visibility = View.VISIBLE
                clSound.visibility = View.VISIBLE
            }
        }
    }

    private fun showDescSurah() {
        val surahDesc = arguments?.getString(SURAH_DESC)
        val htmlFormat = HtmlCompat.fromHtml(surahDesc.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Deskripsi Surah")
        builder.setMessage(htmlFormat)
        builder.setCancelable(false)
        builder.setPositiveButton("Tutup") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    @SuppressLint("NewApi", "InflateParams")
    private fun showFontSettingDialog() {
        val builder = AlertDialog.Builder(requireActivity()).create()
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_font_setting, null)
        val seekBar = dialogLayout.findViewById<SeekBar>(R.id.seekbar_font_size)
        val buttonSave = dialogLayout.findViewById<Button>(R.id.btn_save)
        sbCurrent = seekBar!!
        with(builder) {
            setView(dialogLayout)
            sbCurrent.max = 38
            sbCurrent.min = 18
            sbCurrent.progress = fontSize ?: 24
            sbCurrent.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?, progress: Int, fromUser: Boolean
                ) {
                    fontSize = progress
                    sbCurrent.progress = fontSize!!
                    detailAdapter.setFontSize(fontSize!!)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
            buttonSave.setOnClickListener {
                dismiss()
            }
            setCanceledOnTouchOutside(false)
            show()
        }
    }

    override fun onResume() {
        super.onResume()

        dataStoreViewModel.getAyahSize.observe(viewLifecycleOwner) { size ->
            fontSize = size
            detailAdapter.setFontSize(fontSize!!)
        }
    }

    override fun onPause() {
        super.onPause()

        fontSize?.let { size -> dataStoreViewModel.saveAyahSize(size) }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (audioIsPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }

        _binding = null
    }

    companion object {
        const val SURAH_NUMBER = "surahNumber"
        const val SURAH_NAME = "surahName"
        const val SURAH_DESC = "surahDesc"
    }
}
