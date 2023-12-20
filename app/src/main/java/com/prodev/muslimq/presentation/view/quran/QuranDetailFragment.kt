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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.Ayat
import com.prodev.muslimq.core.data.source.local.model.BookmarkEntity
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.databinding.DialogAudioAyahBinding
import com.prodev.muslimq.databinding.DialogDownloadBinding
import com.prodev.muslimq.databinding.DialogFontSettingBinding
import com.prodev.muslimq.databinding.DialogFontSettingRadioBinding
import com.prodev.muslimq.databinding.DialogInfoSurahBinding
import com.prodev.muslimq.databinding.DialogLoadingBinding
import com.prodev.muslimq.databinding.DialogPlayerActionBinding
import com.prodev.muslimq.databinding.DialogSearchBinding
import com.prodev.muslimq.databinding.DialogTaggingAyahBinding
import com.prodev.muslimq.databinding.FragmentQuranDetailBinding
import com.prodev.muslimq.helper.isOnline
import com.prodev.muslimq.helper.swipeRefresh
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.adapter.QuranDetailAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.BookmarkViewModel
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.QuranDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private val detailViewModel: QuranDetailViewModel by viewModels()
    private val bookmarkViewModel: BookmarkViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    private val args: QuranDetailFragmentArgs by navArgs()

    private val curvedDialog by lazy {
        AlertDialog.Builder(requireContext(), R.style.CurvedDialog)
    }
    private val transparentDialog by lazy {
        AlertDialog.Builder(requireContext(), R.style.TransparentDialog).create()
    }
    private var dialog: AlertDialog? = null
    private var dialogLayout: DialogLoadingBinding? = null
    private var progressDialog: ProgressBar? = null
    private var tvProgress: TextView? = null

    private var audioIsPlaying = false
    private var playOnline = false
    private var fontSize: Int? = null
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
                message = "Izin penyimpanan diberikan"
            )
            checkAudioState(audioGlobal)
        } else {
            (activity as MainActivity).customSnackbar(
                state = false,
                context = requireContext(),
                view = binding.root,
                message = "Izin penyimpanan ditolak"
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
                message = "Izin penyimpanan diberikan"
            )
            checkAudioState(audioGlobal)
        } else {
            (activity as MainActivity).customSnackbar(
                state = false,
                context = requireContext(),
                view = binding.root,
                message = "Izin penyimpanan ditolak"
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        swipeRefresh(
            { detailViewModel.fetchQuranDetail() }, binding.srlSurah
        )
        initAdapter()
        initViewModel()
    }

    private fun initAdapter() {
        detailAdapter = QuranDetailAdapter(
            context = requireActivity(), surahName = binding.tvSurahName.text.toString()
        )

        binding.rvAyah.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detailAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }
    }

    private fun initViewModel() {
        // get quran detail
        detailViewModel.getQuranDetail.observe(viewLifecycleOwner) { result ->
            with(binding) {
                val isLoading = result is Resource.Loading && result.data == null
                val isError = result is Resource.Error && result.data == null
                val isSuccess = result is Resource.Success

                progressHeader.isVisible = isLoading
                progressBar.isVisible = isLoading
                clSurah.isVisible = isSuccess
                clSound.isVisible = isSuccess
                rvAyah.isVisible = isSuccess
                clNoInternet.isVisible = isError

                if (isSuccess) {
                    val dataSurah = result.data!!

                    // set data
                    val ayahs = ArrayList<Ayat>().apply { addAll(dataSurah.ayat) }
                    enableActionBarFunctionality(dataSurah)

                    // set view data
                    val place = dataSurah.tempatTurun.replaceFirstChar { it.uppercase() }
                    val totalAyah = dataSurah.jumlahAyat
                    val nameLatin = dataSurah.namaLatin
                    if (nameLatin.contains("Al-Fatihah")) {
                        vDivider.isVisible = false
                        tvBismillah.isVisible = false
                    }

                    toolbar.title = nameLatin
                    tvSurahName.text = nameLatin
                    tvAyahMeaning.text = dataSurah.artiQuran
                    tvCityAndTotalAyah.text =
                        getString(R.string.tv_city_and_total_ayah, place, totalAyah)

                    // set list
                    val ayahNumber = args.ayahNumber
                    val isFromLastRead = args.isFromLastRead
                    showListAyah(
                        ayahs,
                        appBar,
                        rvAyah,
                        ayahNumber,
                        isFromLastRead
                    )

                    // setup bookmark
                    bookmarkViewModel.setBookmark(dataSurah.surahId)

                    setUpMediaPlayer(dataSurah.audio)
                    initProgressDialog(nameLatin)

                    // setup transparentdialog
                    dialogLayout = DialogLoadingBinding.inflate(layoutInflater)
                    transparentDialog.setView(dialogLayout!!.root)

                    // tagging
                    taggingSurah(dataSurah)

                    // tafsir
                    tafsirSurah()

                    // play audio per ayah
                    playAudioPerAyah()
                }
            }
        }

        // get bookmark state
        bookmarkViewModel.isBookmarked.observe(viewLifecycleOwner) { state ->
            checkBookmarkState(state)

            detailViewModel.getQuranDetail.value?.data?.let { dataSurah ->
                val bookmarkEntity = BookmarkEntity(
                    dataSurah.surahId,
                    dataSurah.nama,
                    dataSurah.namaLatin,
                    dataSurah.deskripsi,
                    dataSurah.jumlahAyat,
                    dataSurah.artiQuran,
                )

                binding.ivBookmark.setOnClickListener {

                    if (state) {
                        bookmarkViewModel.deleteBookmark(bookmarkEntity)
                    } else {
                        bookmarkViewModel.insertBookmark(bookmarkEntity)
                    }

                    val snackbarMessage = if (state) {
                        "Berhasil dihapus dari \"Baca Nanti\""
                    } else {
                        "Berhasil disimpan ke \"Baca Nanti\""
                    }

                    (activity as MainActivity).customSnackbar(
                        state = !state,
                        context = requireContext(),
                        view = binding.root,
                        message = snackbarMessage,
                        action = !state,
                        toOtherFragment = true
                    )
                }
            }
        }

        // get tafsir
        detailViewModel.getQuranTafsir.observe(viewLifecycleOwner) { response ->
            val (result, ayahNumber) = response
            when (result) {
                is Resource.Loading -> {
                    transparentDialog.show()
                }

                is Resource.Success -> {
                    transparentDialog.dismiss()
                    showDescSurah(
                        title = "Tafsir Ayat $ayahNumber",
                        tafsir = result.data!!.teks,
                        isTafsir = true
                    )
                }

                is Resource.Error -> {
                    transparentDialog.dismiss()
                    (activity as MainActivity).customSnackbar(
                        state = false,
                        context = requireContext(),
                        view = binding.root,
                        message = "Tafsir gagal dimuat"
                    )
                }
            }
        }
    }

    private fun playAudioPerAyah() {
        detailAdapter.audioAyahClick = {
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

    private fun tafsirSurah() {
        detailAdapter.tafsirClick = {
            detailViewModel.fetchQuranTafsir(
                ayahNumber = it.ayatNumber
            )
        }
    }

    private fun taggingSurah(dataSurah: QuranDetailEntity) {
        detailAdapter.taggingClick = { data ->
            val dialogLayout = DialogTaggingAyahBinding.inflate(layoutInflater)
            val tvTagging = dialogLayout.tvTagging
            val tvCancel = dialogLayout.tvCancel
            with(curvedDialog.create()) {
                setView(dialogLayout.root)
                tvTagging.setOnClickListener {
                    dataStoreViewModel.saveSurah(
                        dataSurah.surahId,
                        dataSurah.nama,
                        dataSurah.namaLatin,
                        data.ayatNumber
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

    private fun checkBookmarkState(bookmarkStatus: Boolean) {
        binding.apply {
            if (bookmarkStatus) {
                ivBookmark.setImageResource(R.drawable.ic_bookmark_true)
            } else {
                ivBookmark.setImageResource(R.drawable.ic_bookmark_false)
            }
        }
    }

    private fun showListAyah(
        ayahs: ArrayList<Ayat>,
        appBar: AppBarLayout,
        rvAyah: RecyclerView,
        ayahNumber: Int?,
        isFromLastRead: Boolean?
    ) {
        detailAdapter.submitList(ayahs)

        if (ayahNumber != null && isFromLastRead == true) {
            appBar.setExpanded(false, true)
            rvAyah.scrollToPosition(ayahNumber.minus(1))
            detailAdapter.setTagging(true, ayahNumber.minus(1))
            Toast.makeText(
                requireContext(), "Melanjutkan dari ayat $ayahNumber", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun enableActionBarFunctionality(dataSurah: QuranDetailEntity) {
        binding.apply {
            ivFontSetting.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    showFontSettingDialog()
                } else {
                    showFontSettingDialogLower()
                }
            }

            ivMore.setOnClickListener {
                showMenuOption(dataSurah)
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
            sbCurrentFontSize.max = 48
            sbCurrentFontSize.min = 26
            sbCurrentFontSize.progress = fontSize ?: 34
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
        val radioSuperBig = dialogLayout.rbSuperBig
        val buttonSave = dialogLayout.btnSave
        with(curvedDialog.create()) {
            setView(dialogLayout.root)
            when (fontSize) {
                26 -> radioSmall.isChecked = true
                34 -> radioMedium.isChecked = true
                40 -> radioBig.isChecked = true
                48 -> radioSuperBig.isChecked = true
            }
            radioSmall.setOnClickListener {
                fontSize = 26
                detailAdapter.setFontSize(fontSize!!)
                radioMedium.isChecked = false
                radioBig.isChecked = false
                radioSuperBig.isChecked = false
            }
            radioMedium.setOnClickListener {
                fontSize = 34
                detailAdapter.setFontSize(fontSize!!)
                radioSmall.isChecked = false
                radioBig.isChecked = false
                radioSuperBig.isChecked = false
            }
            radioBig.setOnClickListener {
                fontSize = 40
                detailAdapter.setFontSize(fontSize!!)
                radioSmall.isChecked = false
                radioMedium.isChecked = false
                radioSuperBig.isChecked = false
            }
            radioSuperBig.setOnClickListener {
                fontSize = 48
                detailAdapter.setFontSize(fontSize!!)
                radioSmall.isChecked = false
                radioMedium.isChecked = false
                radioBig.isChecked = false
            }
            buttonSave.setOnClickListener {
                dismiss()
            }
            setCanceledOnTouchOutside(false)
            show()
        }
    }

    private fun showMenuOption(dataSurah: QuranDetailEntity) {
        PopupMenu(requireContext(), binding.ivMore).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setForceShowIcon(true)
            }

            menuInflater.inflate(R.menu.menu_detail_quran, menu)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_search -> {
                        showSearchDialog()
                        true
                    }

                    R.id.action_info -> {
                        showDescSurah(
                            title = "Deskripsi Surah",
                            desc = dataSurah.deskripsi,
                            isTafsir = false
                        )
                        true
                    }

                    else -> false
                }
            }
            show()
        }
    }

    private fun showSearchDialog() {
        val dialogLayout = DialogSearchBinding.inflate(layoutInflater)
        val etSearch = dialogLayout.etSearch
        val btnSearch = dialogLayout.btnSearch
        with(curvedDialog.create()) {
            setView(dialogLayout.root)
            etSearch.setOnEditorActionListener { _, _, _ -> btnSearch.performClick() }
            btnSearch.setOnClickListener {
                val query = etSearch.text.toString()
                val position = detailAdapter.currentList.indexOfFirst {
                    it.ayatNumber.toString() == query
                }
                if (position != -1) {
                    binding.apply {
                        appBar.setExpanded(position < 1, true)
                        rvAyah.scrollToPosition(position)
                        detailAdapter.setTagging(true, position)
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
                    false,
                    requireContext(),
                    binding.root,
                    "Izinkan untuk mengakses penyimpanan",
                    true,
                    toSettings = true
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
                    toSettings = true
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
                    toSettings = true
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
                playOnline = false
                playPauseAudio(binding.ivSound, false, mp3File, isFromLocal = true)
            }

            playOnline -> {
                if (isOnline(requireContext())) {
                    playPauseAudio(binding.ivSound, false, audio)
                } else {
                    (activity as MainActivity).customSnackbar(
                        state = false,
                        context = requireContext(),
                        view = binding.root,
                        message = "Tidak ada koneksi internet"
                    )
                }
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
                        dismiss()
                        if (isOnline(requireContext())) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                                downloadAudio(audio)
                            } else {
                                saveToMediaStore(audio)
                            }
                        } else {
                            (activity as MainActivity).customSnackbar(
                                state = false,
                                context = requireContext(),
                                view = binding.root,
                                message = "Tidak ada koneksi internet"
                            )
                        }
                    }
                    tvStreaming.setOnClickListener {
                        dismiss()
                        playOnline = true
                        if (isOnline(requireContext())) {
                            playPauseAudio(binding.ivSound, false, audio)
                            transparentDialog.show()
                        } else {
                            (activity as MainActivity).customSnackbar(
                                state = false,
                                context = requireContext(),
                                view = binding.root,
                                message = "Tidak ada koneksi internet"
                            )
                        }
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
                        message = "Berhasil mengunduh Surah ${binding.tvSurahName.text}"
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
                        message = "Gagal mengunduh Surah ${binding.tvSurahName.text}"
                    )
                }
            }
        }
    }

    private fun playPauseAudio(
        buttonInteract: ImageView,
        isPlay: Boolean,
        audio: String? = null,
        isFromLocal: Boolean = false
    ) {
        if (isPlay) {
            audioIsPlaying = false
            mediaPlayer.pause()
            buttonInteract.setImageResource(R.drawable.ic_play)
        } else {
            audio?.let { checkInitializeMediaPlayer(it) }
            audioIsPlaying = true
            buttonInteract.setImageResource(R.drawable.ic_pause)
            updateSeekbar(isFromLocal)
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
                                message = "Berhasil mengunduh Surah ${binding.tvSurahName.text}"
                            )
                            playPauseAudio(binding.ivSound, false, audio)
                        } else {
                            dialog?.dismiss()
                            (activity as MainActivity).customSnackbar(
                                state = false,
                                context = requireContext(),
                                view = binding.root,
                                message = "Gagal mengunduh Surah ${binding.tvSurahName.text}"
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

    private fun checkInitializeMediaPlayer(mp3File: String) {
        binding.apply {
            if (!this@QuranDetailFragment::mediaPlayer.isInitialized) {
                initMediaPlayer(mp3File)
            } else {
                mediaPlayer.start()
                transparentDialog.dismiss()
            }

            mediaPlayer.apply {
                try {
                    prepareAsync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun initMediaPlayer(mp3File: String) {
        binding.apply {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(mp3File)
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
                    start()
                    playOnline = true
                    ivSound.setImageResource(R.drawable.ic_pause)
                    transparentDialog.dismiss()
                }

                setOnCompletionListener {
                    playOnline = false
                    ivSound.setImageResource(R.drawable.ic_play)
                }

                setOnErrorListener { _, _, _ ->
                    reset()
                    try {
                        setDataSource(mp3File)
                        prepareAsync()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    false
                }
            }
        }
    }

    private fun updateSeekbar(isFromLocal: Boolean) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    binding.sbSound.progress = mediaPlayer.currentPosition
                    handler.postDelayed(this, 1000)
                    val isOnline = isOnline(requireContext())
                    if (!isOnline && playOnline && !isFromLocal) {
                        mediaPlayer.stop()
                        binding.apply {
                            ivSound.setImageResource(R.drawable.ic_play)
                            transparentDialog.dismiss()
                        }

                        playOnline = false
                        (activity as MainActivity).customSnackbar(
                            state = false,
                            context = requireContext(),
                            view = binding.root,
                            message = "Tidak ada koneksi internet"
                        )
                    }
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

    private fun showDescSurah(
        title: String,
        desc: String = "",
        tafsir: String = "",
        isTafsir: Boolean
    ) {
        val htmlTeksParse = if (isTafsir) {
            tafsir
        } else {
            desc
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
        if (audioIsPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        detailViewModel.getQuranDetail.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}