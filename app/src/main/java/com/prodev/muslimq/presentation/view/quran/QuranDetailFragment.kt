package com.prodev.muslimq.presentation.view.quran

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.adapter.QuranDetailAdapter
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import com.simform.refresh.SSPullToRefreshLayout
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
class QuranDetailFragment : Fragment() {

    private lateinit var detailAdapter: QuranDetailAdapter
    private lateinit var sbCurrentFontSize: SeekBar
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var binding: FragmentQuranDetailBinding

    private val detailViewModel: QuranViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    private var audioIsPlaying = false
    private var playOnline = false
    private var fontSize: Int? = null
    private var isFirstLoad = false
    private var isResume = false

    private var surahId: Int? = null
    private var surahName: String = ""
    private var surahMeaning: String = ""
    private var surahDesc: String = ""
    private var config: Configuration = Configuration()
    private var is600dp: Boolean = false
    private var progressDialog: ProgressDialog? = null

    private val requestPermissionStorage = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(requireContext(), "Perizinan diberikan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized) {
            binding
            isFirstLoad = false
        } else {
            binding = FragmentQuranDetailBinding.inflate(inflater, container, false)
            isFirstLoad = true
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isFirstLoad) {
            setAdapter()
            setViewModel()

            config = resources.configuration
            is600dp = config.smallestScreenWidthDp >= 600
        }

        binding.apply {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }

            ivFontSetting.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    showFontSettingDialog()
                } else {
                    showFontSettingDialogLower()
                }
            }

            ivMore.setOnClickListener {
                showMenuOption()
            }
        }

        surahDesc = arguments?.getString(SURAH_DESC) ?: ""

        initProgressDialog()
    }

    private fun initProgressDialog() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.apply {
            setTitle(getString(R.string.download_surah, binding.tvSurahName.text))
            setMessage(getString(R.string.wait_download))
            setCancelable(false)
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        }
    }

    private fun showMenuOption() {
        val mp3File = getString(
            R.string.fileName,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            binding.tvSurahName.text
        )

        val file = File(mp3File)
        val optionDeleteEnabled =
            (file.exists() && (this::mediaPlayer.isInitialized && !mediaPlayer.isPlaying)) || (file.exists() && !this::mediaPlayer.isInitialized)

        PopupMenu(requireContext(), binding.ivMore).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setForceShowIcon(true)
            }

            menuInflater.inflate(R.menu.menu_detail_quran, menu)
            menu.findItem(R.id.action_delete_audio).isEnabled = optionDeleteEnabled

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_info -> {
                        showDescSurah("Deskripsi Surah", surahDesc, false)
                        true
                    }
                    R.id.action_delete_audio -> {
                        val builder =
                            androidx.appcompat.app.AlertDialog.Builder(requireActivity()).create()
                        val dialogLayout = layoutInflater.inflate(R.layout.dialog_delete_all, null)
                        val tvConfirm = dialogLayout.findViewById<TextView>(R.id.tv_confirm)
                        val tvCancel = dialogLayout.findViewById<TextView>(R.id.tv_cancel)
                        with(builder) {
                            setView(dialogLayout)
                            tvConfirm.setOnClickListener {
                                if (file.exists()) {
                                    file.delete()
                                    (activity as MainActivity).customSnackbar(
                                        state = false,
                                        context = requireContext(),
                                        view = binding.root,
                                        message = "${binding.tvSurahName.text} berhasil dihapus."
                                    )
                                }

                                dismiss()
                            }
                            tvCancel.setOnClickListener { dismiss() }
                            setCanceledOnTouchOutside(false)
                            show()
                        }
                        true
                    }
                    else -> false
                }
            }
            show()
        }
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

    @SuppressLint("SetTextI18n")
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
                            toolbar.title = result.data?.namaLatin

                            surahName = result.data!!.namaLatin
                            surahMeaning = result.data!!.artiQuran

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

                            val ayahNumber = arguments?.getInt(AYAH_NUMBER)
                            val isFromLastRead = arguments?.getBoolean(IS_FROM_LAST_READ)

                            val ayahs = ArrayList<Ayat>()
                            result.data?.ayat?.let { ayahs.addAll(it) }
                            if (ayahs[0].ayatTerjemahan.contains("Dengan nama Allah Yang Maha Pengasih, Maha Penyayang")) {
                                ayahs.removeAt(0)
                                detailAdapter.setList(ayahs)
                                if (ayahNumber != null && isFromLastRead == true && !isResume) {
                                    appBar.setExpanded(false, true)
                                    rvAyah.scrollToPosition(ayahNumber.minus(2))
                                    detailAdapter.setTagging(true, ayahNumber.minus(2))
                                    Toast.makeText(
                                        requireContext(),
                                        "Melanjutkan dari ayat $ayahNumber",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isResume = true
                                }
                            } else {
                                detailAdapter.setList(ayahs)
                                if (ayahNumber != null && isFromLastRead == true && !isResume) {
                                    appBar.setExpanded(false, true)
                                    rvAyah.scrollToPosition(ayahNumber.minus(1))
                                    detailAdapter.setTagging(true, ayahNumber.minus(1))
                                    Toast.makeText(
                                        requireContext(),
                                        "Melanjutkan dari ayat $ayahNumber",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isResume = true
                                }
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
                                        (activity as MainActivity).customSnackbar(
                                            state = true,
                                            context = requireContext(),
                                            view = binding.root,
                                            message = "Berhasil ditambahkan ke \"Baca Nanti\"",
                                        )
                                    } else {
                                        (activity as MainActivity).customSnackbar(
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

            detailAdapter.taggingQuran = {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Tandai ayat?")
                    setMessage("Apakah Anda ingin menandai ayat ini sebagai ayat yang terakhir dibaca?")
                    setPositiveButton("Ya") { dialog, _ ->
                        dataStoreViewModel.saveSurah(
                            surahId!!, surahName, surahMeaning, surahDesc, it.ayatNumber
                        )

                        Toast.makeText(
                            requireContext(),
                            "Ayat ${it.ayatNumber} berhasil ditandai",
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }
                    setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    val alertDialog = create()
                    alertDialog.show()
                    if (is600dp) {
                        val lp = WindowManager.LayoutParams()
                        alertDialog.window?.let { window ->
                            lp.copyFrom(window.attributes)
                            lp.width = 600
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                            window.attributes = lp
                        }
                    }
                }
            }

            detailAdapter.tafsirQuran = {
                val progressDialog = ProgressDialog(requireContext())
                progressDialog.setMessage("Memuat...")
                detailViewModel.getQuranTafsir(
                    surahId!!, it.ayatNumber
                ).observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Resource.Loading -> {
                            progressDialog.show()
                        }
                        is Resource.Success -> {
                            progressDialog.dismiss()
                            showDescSurah(
                                "Tafsir Ayat ${it.ayatNumber}", result.data!!.teks, true
                            )
                        }
                        is Resource.Error -> {
                            progressDialog.dismiss()
                            Toast.makeText(
                                requireContext(), "Gagal menampilkan tafsir", Toast.LENGTH_SHORT
                            ).show()
                            Log.e("Gagal Kenapa", result.error.toString())
                        }
                    }
                }
            }
        }
    }

    private fun setUpMediaPlayer(audio: String) {
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
                    isDownload = true
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
                    true,
                    requireContext(),
                    binding.root,
                    "Perizinan > File dan Media > Izinkan akses ke media saja",
                    true,
                    isDownload = true
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

        val permissionReadGranted = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED

        val permissionWriteGranted = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED

        when {
            permissionReadGranted && permissionWriteGranted -> {
                checkAudioState(audio)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                (activity as MainActivity).customSnackbar(
                    true,
                    requireContext(),
                    binding.root,
                    "Izinkan untuk mengakses penyimpanan",
                    true,
                    isDownload = true
                )
            }
            else -> {
                ActivityCompat.requestPermissions(
                    requireActivity(), permissions, 1
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            val write = grantResults[0] == PERMISSION_GRANTED
            val read = grantResults[1] == PERMISSION_GRANTED
            if (write && read) {
                Toast.makeText(requireContext(), "Perizinan diberikan", Toast.LENGTH_SHORT).show()
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
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(getString(R.string.ask_audio_title, binding.tvSurahName.text))
                    setMessage(getString(R.string.ask_audio_desc, binding.tvSurahName.text))
                    setPositiveButton(getString(R.string.download)) { _, _ ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            saveToMediaStore(audio)
                        } else {
                            downloadAudio(audio, mp3File)
                        }
                    }
                    setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    setNeutralButton(getString(R.string.play_online)) { _, _ ->
                        playOnline = true
                        playPauseAudio(binding.ivSound, false, audio)
                    }
                    val alertDialog = create()
                    alertDialog.show()
                    if (is600dp) {
                        val lp = WindowManager.LayoutParams()
                        alertDialog.window?.let { window ->
                            lp.copyFrom(window.attributes)
                            lp.width = 600
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                            window.attributes = lp
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToMediaStore(audioUrl: String) {
        val relativeLocation = "${Environment.DIRECTORY_DOWNLOADS}/MuslimQ"
        progressDialog?.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.IO) {
                    val connection = URL(audioUrl).openConnection() as HttpURLConnection
                    connection.connect()


                    val fileSize = connection.contentLength
                    val inputStream = BufferedInputStream(connection.inputStream)

                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, "${binding.tvSurahName.text}.mp3")
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
                                progressDialog?.progress = (downloadedBytes * 100 / fileSize)
                            }

                            outputStream.flush()
                            outputStream.close()
                            inputStream.close()
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    progressDialog?.dismiss()
                    Toast.makeText(requireContext(), "Audio saved successfully", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog?.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Error saving audio: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Error saving audio", e.localizedMessage.toString())
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

    private fun downloadAudio(audio: String, mp3FileLocation: String) {
        progressDialog?.show()

        val request = DownloadManager.Request(Uri.parse(audio))
            .setTitle(getString(R.string.download_surah_notif, binding.tvSurahName.text))
            .setDescription(getString(R.string.download_surah, binding.tvSurahName.text))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true).setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, "MuslimQ/${binding.tvSurahName.text}.mp3"
            )

        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val downloadCompleteReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                            progressDialog?.dismiss()
                            playPauseAudio(binding.ivSound, false, mp3FileLocation)
                        }
                    }
                    cursor.close()
                }
            }
        }

        requireActivity().registerReceiver(
            downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        val query = DownloadManager.Query().setFilterById(downloadId)
        val progressHandler = Handler(Looper.getMainLooper())
        progressHandler.post(object : Runnable {
            override fun run() {
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                        progressDialog?.dismiss()
                    } else {
                        val progressIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val totalIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val progress = cursor.getLong(progressIndex)
                        val total = cursor.getLong(totalIndex)
                        if (total >= 0) {
                            progressDialog?.max = (total / (1024 * 1024)).toInt()
                            progressDialog?.progress = (progress / (1024 * 1024)).toInt()
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
            setDataSource(mp3File)
            prepare()

            with(binding) {
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
                    binding.sbSound.progress = 0
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

    private fun showDescSurah(title: String, tafsir: String, isTafsir: Boolean) {
        val htmlTeksParse = if (isTafsir) {
            tafsir
        } else {
            surahDesc
        }
        val htmlFormat = HtmlCompat.fromHtml(htmlTeksParse, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(title)
        builder.setMessage(htmlFormat)
        builder.setCancelable(false)
        builder.setPositiveButton("Tutup") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
        if (is600dp) {
            val lp = WindowManager.LayoutParams()
            alertDialog.window?.let { window ->
                lp.copyFrom(window.attributes)
                lp.width = 600
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                window.attributes = lp
            }
        }
    }

    @SuppressLint("NewApi", "InflateParams")
    private fun showFontSettingDialog() {
        val builder = AlertDialog.Builder(requireActivity()).create()
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_font_setting, null)
        val seekBar = dialogLayout.findViewById<SeekBar>(R.id.seekbar_font_size)
        val buttonSave = dialogLayout.findViewById<Button>(R.id.btn_save)
        seekBar?.let { sbCurrentFontSize = it }
        with(builder) {
            setView(dialogLayout)
            sbCurrentFontSize.max = 38
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
        val builder = AlertDialog.Builder(requireActivity()).create()
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_font_setting_radio, null)
        val radioSmall = dialogLayout.findViewById<RadioButton>(R.id.rb_small)
        val radioMedium = dialogLayout.findViewById<RadioButton>(R.id.rb_medium)
        val radioBig = dialogLayout.findViewById<RadioButton>(R.id.rb_big)
        val buttonSave = dialogLayout.findViewById<Button>(R.id.btn_save)
        with(builder) {
            setView(dialogLayout)
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

    override fun onDestroy() {
        super.onDestroy()

        if (audioIsPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    companion object {
        const val SURAH_NAME = "surahName"
        const val SURAH_NUMBER = "surahNumber"
        const val SURAH_DESC = "surahDesc"
        const val AYAH_NUMBER = "ayahNumber"
        const val IS_FROM_LAST_READ = "isFromLastRead"
    }
}