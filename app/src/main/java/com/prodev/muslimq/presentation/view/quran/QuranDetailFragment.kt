package com.prodev.muslimq.presentation.view.quran

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.Ayat
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.isOnline
import com.prodev.muslimq.databinding.DialogAudioAyahBinding
import com.prodev.muslimq.databinding.DialogDownloadBinding
import com.prodev.muslimq.databinding.DialogFontSettingBinding
import com.prodev.muslimq.databinding.DialogFontSettingRadioBinding
import com.prodev.muslimq.databinding.DialogInfoSurahBinding
import com.prodev.muslimq.databinding.DialogLoadingBinding
import com.prodev.muslimq.databinding.DialogPlayerActionBinding
import com.prodev.muslimq.databinding.DialogSearchAyahBinding
import com.prodev.muslimq.databinding.DialogTaggingAyahBinding
import com.prodev.muslimq.databinding.FragmentQuranDetailBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.adapter.QuranDetailAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@AndroidEntryPoint
class QuranDetailFragment :
    BaseFragment<FragmentQuranDetailBinding>(FragmentQuranDetailBinding::inflate) {

    private lateinit var detailAdapter: QuranDetailAdapter
    private lateinit var sbCurrentFontSize: SeekBar
    private lateinit var mediaPlayer: MediaPlayer

    private val detailViewModel: QuranViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    private val curvedDialog by lazy {
        AlertDialog.Builder(requireContext(), R.style.CurvedDialog)
    }
    private val transparentDialog by lazy {
        AlertDialog.Builder(requireContext(), R.style.TransparentDialog).create()
    }
    private var dialog: AlertDialog? = null
    private var progressDialog: ProgressBar? = null
    private var tvProgress: TextView? = null

    private var audioIsPlaying = false
    private var playOnline = false
    private var fontSize: Int? = null
    private var isResume = false
    private var sizeHasDone = false

    private var surahId: Int? = null
    private var surahNameArabic: String = ""
    private var surahName: String = ""
    private var surahDesc: String = ""
    private var currentPage = 1
    private var audioGlobal = ""

    @RequiresApi(Build.VERSION_CODES.R)
    private val requestPermissionStorage = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            (activity as MainActivity).customSnackbar(
                state = true,
                context = requireContext(),
                view = binding.root,
                message = "Izin penyimpanan diberikan",
                isDetailScreen = true
            )
            checkAudioState(audioGlobal)
        } else {
            (activity as MainActivity).customSnackbar(
                state = false,
                context = requireContext(),
                view = binding.root,
                message = "Izin penyimpanan ditolak",
                isDetailScreen = true
            )
        }
    }

    private val requestPermissionStorageLower = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        val permissionGranted = permission[Manifest.permission.READ_EXTERNAL_STORAGE] == true ||
                permission[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true

        if (permissionGranted) {
            (activity as MainActivity).customSnackbar(
                state = true,
                context = requireContext(),
                view = binding.root,
                message = "Izin penyimpanan diberikan",
                isDetailScreen = true
            )
            checkAudioState(audioGlobal)
        } else {
            (activity as MainActivity).customSnackbar(
                state = false,
                context = requireContext(),
                view = binding.root,
                message = "Izin penyimpanan ditolak",
                isDetailScreen = true
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        surahDesc = arguments?.getString(SURAH_DESC) ?: ""

        setAdapter()
        setViewModel()
    }

    private fun setAdapter() {
        detailAdapter = QuranDetailAdapter(
            context = requireActivity(), surahName = arguments?.getString(SURAH_NAME) ?: ""
        )

        binding.rvAyah.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detailAdapter
            setHasFixedSize(true)
        }
    }

    private fun setViewModel() {
        val id = arguments?.getInt(SURAH_NUMBER)
        id?.let { idSurah ->
            surahId = idSurah
            detailViewModel.getQuranDetail(surahId!!).observe(viewLifecycleOwner) { result ->
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

                    val isLoading = result is Resource.Loading && result.data == null
                    val isError = result is Resource.Error && result.data == null

                    if (isLoading) {
                        stateLoading(true)
                    } else if (isError) {
                        stateLoading(false)
                        clNoInternet.visibility = View.VISIBLE
                        clSurah.visibility = View.GONE
                        clSound.visibility = View.GONE
                    } else {
                        stateLoading(false)
                        rvAyah.visibility = View.VISIBLE
                        clNoInternet.visibility = View.GONE

                        val dataSurah = result.data!!

                        // set data
                        val ayahs = ArrayList<Ayat>().apply { addAll(dataSurah.ayat) }
                        enableActionBarFunctionality(ayahs)

                        // set view data
                        val place = dataSurah.tempatTurun.replaceFirstChar { it.uppercase() }
                        val totalAyah = dataSurah.jumlahAyat
                        val toolbarTitle = dataSurah.namaLatin

                        surahNameArabic = dataSurah.nama
                        surahName = dataSurah.namaLatin
                        toolbar.title = toolbarTitle
                        tvSurahName.text = dataSurah.namaLatin
                        tvAyahMeaning.text = dataSurah.artiQuran
                        tvCityAndTotalAyah.text =
                            getString(R.string.tv_city_and_total_ayah, place, totalAyah)

                        // set list
                        val ayahNumber = arguments?.getInt(AYAH_NUMBER)
                        val isFromLastRead = arguments?.getBoolean(IS_FROM_LAST_READ)
                        val isAlfatihah = dataSurah.ayat[0].ayatNumber.toString().startsWith("2")

                        showListAyah(
                            ayahs,
                            isAlfatihah,
                            appBar,
                            rvAyah,
                            ayahNumber,
                            isFromLastRead
                        )

                        // setup bookmark
                        var bookmarked = dataSurah.isBookmarked
                        setBookmark(bookmarked)

                        ivBookmark.setOnClickListener {
                            bookmarked = !bookmarked
                            setBookmark(bookmarked)
                            detailViewModel.insertToBookmark(dataSurah, bookmarked)

                            val snackbarMessage = if (bookmarked) {
                                "Berhasil ditambahkan ke \"Baca Nanti\""
                            } else {
                                "Berhasil dihapus dari \"Baca Nanti\""
                            }

                            (activity as MainActivity).customSnackbar(
                                state = bookmarked,
                                context = requireContext(),
                                view = binding.root,
                                message = snackbarMessage,
                                action = bookmarked,
                                isDetailScreen = true
                            )
                        }

                        // setup sound
                        val isOnline = isOnline(requireContext())
                        clSound.visibility = if (isOnline) View.VISIBLE else View.GONE
                        if (isOnline) {
                            setUpMediaPlayer(dataSurah.audio)
                        }

                        initProgressDialog(surahName)
                    }
                }
            }

            // tagging
            detailAdapter.taggingQuran = { data ->
                val dialogLayout = DialogTaggingAyahBinding.inflate(layoutInflater)
                val tvTagging = dialogLayout.tvTagging
                val tvCancel = dialogLayout.tvCancel
                with(curvedDialog.create()) {
                    setView(dialogLayout.root)
                    tvTagging.setOnClickListener {
                        dataStoreViewModel.saveSurah(
                            surahId!!, surahNameArabic, surahName, surahDesc, data.ayatNumber
                        )

                        Toast.makeText(
                            requireContext(),
                            "Ayat ${data.ayatNumber} berhasil ditandai",
                            Toast.LENGTH_SHORT
                        ).show()
                        dismiss()
                        findNavController().popBackStack()
                    }
                    tvCancel.setOnClickListener {
                        dismiss()
                    }
                    show()
                }
            }

            // tafsir
            detailAdapter.tafsirQuran = {
                val dialogLayout = DialogLoadingBinding.inflate(layoutInflater)
                transparentDialog.setView(dialogLayout.root)

                detailViewModel.getQuranTafsir(
                    surahId!!, it.ayatNumber
                ).observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Resource.Loading -> {
                            transparentDialog.show()
                        }

                        is Resource.Success -> {
                            transparentDialog.dismiss()
                            showDescSurah(
                                "Tafsir Ayat ${it.ayatNumber}", result.data!!.teks, true
                            )
                        }

                        is Resource.Error -> {
                            transparentDialog.dismiss()
                            Toast.makeText(
                                requireContext(), "Gagal menampilkan tafsir", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            // play audio per ayah
            detailAdapter.audioAyah = {
                if (audioIsPlaying) {
                    mediaPlayer.pause()
                    binding.ivSound.setImageResource(R.drawable.ic_play)
                }
                val dialogLayout = DialogAudioAyahBinding.inflate(layoutInflater)
                val tvAyahArabic = dialogLayout.tvAyahArabic
                val tvAyahLatin = dialogLayout.tvAyahLatin
                val tvAudioClose = dialogLayout.tvAudioClose

                if (isOnline(requireContext())) {
                    with(curvedDialog.create()) {
                        tvAyahArabic.text = it.ayatArab
                        tvAyahLatin.text = it.ayatLatin
                        val mpAyah = MediaPlayer.create(requireContext(), Uri.parse(it.ayatAudio))
                        mpAyah.apply {
                            setOnPreparedListener {
                                isLooping = false
                                start()
                            }
                            setOnCompletionListener { mpAyah.stop() }
                            setOnErrorListener { _, _, _ ->
                                mpAyah.stop()
                                mpAyah.release()
                                dismiss()
                                Toast.makeText(
                                    requireContext(), "Gagal memutar audio", Toast.LENGTH_SHORT
                                ).show()
                                false
                            }
                        }
                        tvAudioClose.setOnClickListener { dismiss() }
                        setOnDismissListener { mpAyah.release() }
                        setView(dialogLayout.root)
                        setCanceledOnTouchOutside(false)
                        show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(), "Tidak ada koneksi internet", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun initProgressDialog(surahName: String) {
        val dialogLayout = DialogDownloadBinding.inflate(layoutInflater)
        val tvTitle = dialogLayout.tvTitleDownload
        tvTitle.text = getString(R.string.download_surah, surahName)
        progressDialog = dialogLayout.progressBar
        tvProgress = dialogLayout.tvProgress
        dialog = curvedDialog.create().apply {
            setView(dialogLayout.root)
            setCancelable(false)
        }
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

    private fun showListAyah(
        ayahs: ArrayList<Ayat>,
        isAlfatihah: Boolean,
        appBar: AppBarLayout,
        rvAyah: RecyclerView,
        ayahNumber: Int?,
        isFromLastRead: Boolean?
    ) {
        detailAdapter.setList(ayahs, true)
//        if (!sizeHasDone) {
//            detailAdapter.setList(ayahs.subList(0, 3))
//        }
//        showPagination(ayahs, rvAyah)

        val index = if (isAlfatihah) 2 else 1
        if (ayahNumber != null && isFromLastRead == true && !isResume) {
            detailAdapter.setList(ayahs, true)
            appBar.setExpanded(false, true)
            rvAyah.scrollToPosition(ayahNumber.minus(index))
            detailAdapter.setAnimItem(true, ayahNumber.minus(index))
            Toast.makeText(
                requireContext(), "Melanjutkan dari ayat $ayahNumber", Toast.LENGTH_SHORT
            ).show()
            isResume = true
        }
    }

    private fun showPagination(ayahs: ArrayList<Ayat>, rvAyah: RecyclerView) {
        val dataSize = ayahs.size
        val layoutManager = (rvAyah.layoutManager as LinearLayoutManager)

        with(rvAyah) {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!detailAdapter.getLoading() && (visibleItemCount + firstVisibleItemPosition)
                        >= totalItemCount && firstVisibleItemPosition >= 0 && !sizeHasDone
                    ) {
                        // Show the progress bar and load more data
                        if (totalItemCount != dataSize && dataSize != 3) {
                            detailAdapter.showLoading()
                            loadMoreData(ayahs.subList(3, dataSize))
                        } else {
                            sizeHasDone = true
                        }
                    }
                }
            })
        }
    }

    private fun loadMoreData(ayahs: List<Ayat>) {
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                delay(200)
                detailAdapter.hideLoading()
                val newData = generateNewData(ayahs, currentPage)
                detailAdapter.setList(newData)
                currentPage++
            }
        }
    }

    private fun generateNewData(ayahs: List<Ayat>, currentPage: Int): List<Ayat> {
        val startIndex = (currentPage - 1) * 3
        return ayahs.drop(startIndex).take(3)
    }

    private fun enableActionBarFunctionality(ayahs: ArrayList<Ayat>) {
        binding.apply {
            ivFontSetting.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    showFontSettingDialog()
                } else {
                    showFontSettingDialogLower()
                }
            }

            ivMore.setOnClickListener {
                showMenuOption(ayahs)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showFontSettingDialog() {
        val dialogLayout = DialogFontSettingBinding.inflate(layoutInflater)
        val seekBar = dialogLayout.seekbarFontSize
        val buttonSave = dialogLayout.btnSave
        seekBar.let { sbCurrentFontSize = it }
        with(curvedDialog.create()) {
            setView(dialogLayout.root)
            sbCurrentFontSize.max = 40
            sbCurrentFontSize.min = 20
            sbCurrentFontSize.progress = fontSize ?: 26
            sbCurrentFontSize.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?, progress: Int, fromUser: Boolean
                ) {
                    if (fromUser) {
                        fontSize = progress
                        sbCurrentFontSize.progress = fontSize!!
                        detailAdapter.setFontSize(fontSize!!)
                    }
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

    private fun showFontSettingDialogLower() {
        val dialogLayout = DialogFontSettingRadioBinding.inflate(layoutInflater)
        val radioSmall = dialogLayout.rbSmall
        val radioMedium = dialogLayout.rbMedium
        val radioBig = dialogLayout.rbBig
        val buttonSave = dialogLayout.btnSave
        with(curvedDialog.create()) {
            setView(dialogLayout.root)
            when (fontSize) {
                20 -> radioSmall.isChecked = true
                26 -> radioMedium.isChecked = true
                32 -> radioBig.isChecked = true
            }
            radioSmall.setOnClickListener {
                fontSize = 20
                detailAdapter.setFontSize(fontSize!!)
                radioMedium.isChecked = false
                radioBig.isChecked = false
            }
            radioMedium.setOnClickListener {
                fontSize = 26
                detailAdapter.setFontSize(fontSize!!)
                radioSmall.isChecked = false
                radioBig.isChecked = false
            }
            radioBig.setOnClickListener {
                fontSize = 32
                detailAdapter.setFontSize(fontSize!!)
                radioSmall.isChecked = false
                radioMedium.isChecked = false
            }
            buttonSave.setOnClickListener {
                dismiss()
            }
            setCanceledOnTouchOutside(false)
            show()
        }
    }

    private fun showMenuOption(ayahs: ArrayList<Ayat>) {
        PopupMenu(requireContext(), binding.ivMore).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setForceShowIcon(true)
            }

            menuInflater.inflate(R.menu.menu_detail_quran, menu)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_search -> {
                        showSearchDialog(ayahs)
                        true
                    }

                    R.id.action_info -> {
                        showDescSurah("Deskripsi Surah", surahDesc, false)
                        true
                    }

                    else -> false
                }
            }
            show()
        }
    }

    private fun showSearchDialog(ayahs: ArrayList<Ayat>) {
        val dialogLayout = DialogSearchAyahBinding.inflate(layoutInflater)
        val etSearch = dialogLayout.etAyah
        val btnSearch = dialogLayout.btnSearch
        with(curvedDialog.create()) {
            setView(dialogLayout.root)
            etSearch.setOnEditorActionListener { _, _, _ -> btnSearch.performClick() }
            btnSearch.setOnClickListener {
                detailAdapter.setList(ayahs, true)
                val query = etSearch.text.toString()
                val position = detailAdapter.getAyahs().indexOfFirst {
                    it.ayatNumber.toString() == query
                }
                if (position != -1) {
                    binding.apply {
                        appBar.setExpanded(position < 1, true)
                        rvAyah.scrollToPosition(position)
                        detailAdapter.setAnimItem(true, position)
                    }
                    dismiss()
                } else {
                    Toast.makeText(
                        requireContext(), "Ayat tidak ditemukan", Toast.LENGTH_SHORT
                    ).show()
                }
            }
            show()
        }
    }

    private fun setUpMediaPlayer(audio: String) {
        audioGlobal = audio
        with(binding) {
            ivSound.setOnClickListener {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                        checkPermissionStorageAndroidT(audio)
                    }

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        checkPermissionStorageAndroidR(audio)
                    }

                    else -> {
                        checkPermissionStorageLower(audio)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionStorageAndroidT(audio: String) {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.READ_MEDIA_AUDIO
            ) == PERMISSION_GRANTED -> {
                checkAudioState(audio)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_AUDIO) -> {
                (activity as MainActivity).customSnackbar(
                    true,
                    requireContext(),
                    binding.root,
                    "Izinkan untuk mengakses penyimpanan",
                    true,
                    toSettings = true,
                    isDetailScreen = true
                )
            }

            else -> {
                requestPermissionStorage.launch(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkPermissionStorageAndroidR(audio: String) {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PERMISSION_GRANTED -> {
                checkAudioState(audio)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                (activity as MainActivity).customSnackbar(
                    state = false,
                    context = requireContext(),
                    view = binding.root,
                    message = "Izinkan penyimpanan untuk mengakses fitur ini",
                    action = true,
                    toSettings = true,
                    isDetailScreen = true
                )
            }

            else -> {
                requestPermissionStorage.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun checkPermissionStorageLower(audio: String) {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionStorageGranted = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED

        when {
            permissionStorageGranted -> {
                checkAudioState(audio)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                (activity as MainActivity).customSnackbar(
                    state = false,
                    context = requireContext(),
                    view = binding.root,
                    message = "Izinkan penyimpanan untuk mengakses fitur ini",
                    action = true,
                    toSettings = true,
                    isDetailScreen = true
                )
            }

            else -> {
                requestPermissionStorageLower.launch(permissions)
            }
        }
    }

    private fun checkAudioState(audio: String) {
        val mp3File = getString(
            R.string.fileName,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            binding.tvSurahName.text
        )
        val file = File(mp3File)

        when {
            this@QuranDetailFragment::mediaPlayer.isInitialized && mediaPlayer.isPlaying -> {
                playPauseAudio(binding.ivSound, true)
            }

            file.exists() -> {
                playPauseAudio(binding.ivSound, false, mp3File)
            }

            playOnline -> {
                playPauseAudio(binding.ivSound, false, audio)
            }

            else -> {
                val dialogLayout = DialogPlayerActionBinding.inflate(layoutInflater)
                val tvTitle = dialogLayout.tvPlayerTitle
                val tvMessage = dialogLayout.tvPlayerMessage
                val tvDownload = dialogLayout.tvDownload
                val tvStreaming = dialogLayout.tvStreaming
                val tvCancel = dialogLayout.tvCancel
                tvTitle.text = getString(R.string.ask_audio_title, binding.tvSurahName.text)
                tvMessage.text = getString(R.string.ask_audio_desc, binding.tvSurahName.text)
                with(curvedDialog.create()) {
                    setView(dialogLayout.root)
                    show()
                    tvDownload.setOnClickListener {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                            downloadAudio(audio)
                        } else {
                            saveToMediaStore(audio)
                        }
                        dismiss()
                    }
                    tvStreaming.setOnClickListener {
                        playOnline = true
                        playPauseAudio(binding.ivSound, false, audio)
                        dismiss()
                    }
                    tvCancel.setOnClickListener { dismiss() }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun saveToMediaStore(audioUrl: String) {
        val mp3File = getString(
            R.string.fileName,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            binding.tvSurahName.text
        )
        val file = File(mp3File)
        val relativeLocation = "${Environment.DIRECTORY_DOWNLOADS}/MuslimQ"
        dialog?.show()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                withContext(Dispatchers.IO) {
                    val connection = URL(audioUrl).openConnection() as HttpURLConnection
                    connection.connect()

                    val fileSize = connection.contentLength
                    val inputStream = BufferedInputStream(connection.inputStream)

                    val values = ContentValues().apply {
                        put(
                            MediaStore.Downloads.DISPLAY_NAME, "${binding.tvSurahName.text}.mp3"
                        )
                        put(MediaStore.Downloads.MIME_TYPE, "audio/mpeg")
                        put(MediaStore.Downloads.RELATIVE_PATH, relativeLocation)
                    }

                    val resolver = requireContext().contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    uri?.let {
                        val outputStream = resolver.openOutputStream(uri)
                        outputStream?.let {
                            val buffer = ByteArray(1024)
                            var len = inputStream.read(buffer)
                            var downloadedBytes = 0

                            while (len != -1) {
                                outputStream.write(buffer, 0, len)
                                downloadedBytes += len
                                len = inputStream.read(buffer)
                                withContext(Dispatchers.Main) {
                                    progressDialog?.progress = (downloadedBytes * 100 / fileSize)
                                    tvProgress?.text = "${progressDialog?.progress}%"
                                }
                            }

                            outputStream.flush()
                            outputStream.close()
                            inputStream.close()
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    dialog?.dismiss()
                    (activity as MainActivity).customSnackbar(
                        state = true,
                        context = requireContext(),
                        view = binding.root,
                        message = "Berhasil mengunduh Surah ${binding.tvSurahName.text}",
                        isDetailScreen = true
                    )
                    playPauseAudio(binding.ivSound, false, audioUrl)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (file.exists()) file.delete()
                    dialog?.dismiss()
                    (activity as MainActivity).customSnackbar(
                        state = false,
                        context = requireContext(),
                        view = binding.root,
                        message = "Gagal mengunduh Surah ${binding.tvSurahName.text}",
                        isDetailScreen = true
                    )
                }
            }
        }
    }

    private fun playPauseAudio(
        buttonInteract: ImageView, isPlay: Boolean, audio: String = ""
    ) {
        if (isPlay) {
            audioIsPlaying = false
            mediaPlayer.pause()
            buttonInteract.setImageResource(R.drawable.ic_play)
        } else {
            if (!this::mediaPlayer.isInitialized) {
                initializeMediaPlayer(audio)
            }
            audioIsPlaying = true
            mediaPlayer.start()
            buttonInteract.setImageResource(R.drawable.ic_pause)
            updateSeekbar()
        }
    }

    private fun downloadAudio(audio: String) {
        dialog?.show()

        val request = DownloadManager.Request(Uri.parse(audio))
            .setTitle(getString(R.string.download_surah_notif, binding.tvSurahName.text))
            .setDescription(getString(R.string.download_surah, binding.tvSurahName.text))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true).setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, "MuslimQ/${binding.tvSurahName.text}.mp3"
            )

        val downloadManager =
            context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val downloadCompleteReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val status = cursor.getInt(columnIndex)
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            dialog?.dismiss()
                            (activity as MainActivity).customSnackbar(
                                state = true,
                                context = requireContext(),
                                view = binding.root,
                                message = "Berhasil mengunduh Surah ${binding.tvSurahName.text}",
                                isDetailScreen = true
                            )
                            playPauseAudio(binding.ivSound, false, audio)
                        } else {
                            dialog?.dismiss()
                            (activity as MainActivity).customSnackbar(
                                state = false,
                                context = requireContext(),
                                view = binding.root,
                                message = "Gagal mengunduh Surah ${binding.tvSurahName.text}",
                                isDetailScreen = true
                            )
                        }
                    }

                    cursor.close()
                    requireContext().unregisterReceiver(this)
                }
            }
        }

        requireContext().registerReceiver(
            downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        val query = DownloadManager.Query().setFilterById(downloadId)
        val progressHandler = Handler(Looper.getMainLooper())
        progressHandler.post(object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                        dialog?.dismiss()
                    } else {
                        val progressIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val totalIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val progress = cursor.getLong(progressIndex)
                        val total = cursor.getLong(totalIndex)
                        if (total >= 0) {
                            progressDialog?.progress = (progress * 100 / total).toInt()
                            tvProgress?.text = "${progressDialog?.progress}%"
                        }
                    }
                }
                cursor.close()
                progressHandler.postDelayed(this, 100)
            }
        })
    }

    private fun initializeMediaPlayer(mp3File: String) {
        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {

            with(binding) {
                try {
                    setOnPreparedListener {
                        sbSound.max = duration

                        var currentProgress = currentPosition
                        val audioDuration = duration
                        durationText(tvSoundDuration, currentProgress, audioDuration)
                        sbSound.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seekBar: SeekBar?, progress: Int, fromUser: Boolean
                            ) {
                                if (fromUser) {
                                    seekTo(progress)
                                }

                                currentProgress = progress
                                durationText(tvSoundDuration, currentProgress, audioDuration)
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                        })
                    }

                    setDataSource(mp3File)
                    prepare()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                setOnCompletionListener {
                    playOnline = false
                    ivSound.setImageResource(R.drawable.ic_play)
                }
            }
        }
    }

    private fun updateSeekbar() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    binding.sbSound.progress = mediaPlayer.currentPosition
                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    handler.removeCallbacks(this)
                }
            }
        }, 0)
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

    private fun showDescSurah(title: String, tafsir: String, isTafsir: Boolean) {
        val htmlTeksParse = if (isTafsir) {
            tafsir
        } else {
            surahDesc
        }
        val htmlFormat = HtmlCompat.fromHtml(htmlTeksParse, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val dialogLayout = DialogInfoSurahBinding.inflate(layoutInflater)
        val tvInfoTitle = dialogLayout.tvInfoTitle
        val tvInfoMessage = dialogLayout.tvInfoMessage
        val tvInfoClose = dialogLayout.tvInfoClose
        tvInfoTitle.text = title
        tvInfoMessage.text = htmlFormat
        with(curvedDialog.create()) {
            setView(dialogLayout.root)
            setCanceledOnTouchOutside(false)
            show()
            tvInfoClose.setOnClickListener { dismiss() }
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
        detailViewModel.getQuranDetail(surahId!!).removeObservers(viewLifecycleOwner)
    }

    companion object {
        const val SURAH_NAME = "surahName"
        const val SURAH_NUMBER = "surahNumber"
        const val SURAH_DESC = "surahDesc"
        const val AYAH_NUMBER = "ayahNumber"
        const val IS_FROM_LAST_READ = "isFromLastRead"
    }
}