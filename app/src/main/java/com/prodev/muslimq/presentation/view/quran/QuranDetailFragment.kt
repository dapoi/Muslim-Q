package com.prodev.muslimq.presentation.view.quran

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.prodev.muslimq.presentation.BaseActivity
import com.prodev.muslimq.presentation.adapter.QuranDetailAdapter
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.*


@AndroidEntryPoint
class QuranDetailFragment : Fragment() {

    private lateinit var detailAdapter: QuranDetailAdapter
    private lateinit var sbCurrent: SeekBar
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

    private val requestPermissionStorageTiramisu = registerForActivityResult(
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
        }

        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            ivFontSetting.setOnClickListener {
                showFontSettingDialog()
            }

            ivMore.setOnClickListener {
                showMenuOption()
            }
        }

        surahDesc = arguments?.getString(SURAH_DESC) ?: ""
    }

    private fun showMenuOption() {
        val mp3File = getString(
            R.string.fileName, Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ), binding.tvSurahName.text
        )
        val file = File(mp3File)
        val optionDeleteEnabled = (file.exists() && (this::mediaPlayer.isInitialized &&
                !mediaPlayer.isPlaying)) || (file.exists() && !this::mediaPlayer.isInitialized)

        PopupMenu(requireContext(), binding.ivMore).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setForceShowIcon(true)
            }
            menuInflater.inflate(R.menu.menu_detail_quran, menu)
            menu.findItem(R.id.action_delete_audio).isEnabled = optionDeleteEnabled
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_info -> {
                        showDescSurah()
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
                                    (activity as BaseActivity).customSnackbar(
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
            requireActivity(),
            arguments?.getString(SURAH_NAME) ?: "",
        ) {
            dataStoreViewModel.saveSurah(
                surahId!!,
                surahName,
                surahMeaning,
                surahDesc,
                it.ayatNumber
            )
        }
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
                                        (activity as BaseActivity).customSnackbar(
                                            state = true,
                                            context = requireContext(),
                                            view = binding.root,
                                            message = "Berhasil ditambahkan ke \"Baca Nanti\"",
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
        with(binding) {
            ivSound.setOnClickListener {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                        checkPermissionStorageAndroidTiramisu(audio)
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        checkPermissionStorageR(audio)
                    }
                    else -> {
                        checkPermissionStorage(audio)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionStorageAndroidTiramisu(audio: String) {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkAudioState(audio)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_AUDIO) -> {
                (activity as BaseActivity).customSnackbar(
                    true,
                    requireContext(),
                    binding.root,
                    "Izinkan untuk mengakses penyimpanan",
                    true,
                    isDownload = true
                )
            }
            else -> {
                requestPermissionStorageTiramisu.launch(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkPermissionStorageR(audio: String) {
        if (Environment.isExternalStorageManager()) {
            checkAudioState(audio)
        } else {
            Toast.makeText(
                requireContext(),
                "Izinkan untuk mengakses penyimpanan",
                Toast.LENGTH_SHORT
            ).show()
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            }
        }
    }

    private fun checkPermissionStorage(audio: String) {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionReadGranted = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val permissionWriteGranted = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        when {
            permissionReadGranted && permissionWriteGranted -> {
                checkAudioState(audio)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                (activity as BaseActivity).customSnackbar(
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
                    requireActivity(),
                    permissions,
                    1
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
            val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
            if (write && read) {
                Toast.makeText(requireContext(), "Perizinan diberikan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAudioState(audio: String) {
        val mp3File = getString(
            R.string.fileName, Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ), binding.tvSurahName.text
        )

        when {
            this@QuranDetailFragment::mediaPlayer.isInitialized && mediaPlayer.isPlaying -> {
                playPauseAudio(binding.ivSound, true)
            }
            File(mp3File).exists() -> {
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
                        downloadAudio(audio, mp3File)
                    }
                    setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    setNeutralButton(getString(R.string.play_online)) { _, _ ->
                        playOnline = true
                        playPauseAudio(binding.ivSound, false, audio)
                    }
                    show()
                }
            }
        }
    }

    private fun playPauseAudio(
        buttonInteract: ImageView,
        isPlay: Boolean,
        audio: String = ""
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
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle(getString(R.string.download_surah, binding.tvSurahName.text))
        progressDialog.setMessage(getString(R.string.wait_download))
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.show()

        val request = DownloadManager.Request(Uri.parse(audio))
            .setTitle(getString(R.string.download_surah_notif, binding.tvSurahName.text))
            .setDescription(getString(R.string.download_surah, binding.tvSurahName.text))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "MuslimQ/${binding.tvSurahName.text}.mp3"
            )

        val downloadManager =
            context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val downloadCompleteReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                            progressDialog.dismiss()
                            playPauseAudio(binding.ivSound, false, mp3FileLocation)
                        }
                    }
                    cursor.close()
                }
            }
        }

        requireActivity().registerReceiver(
            downloadCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        val query = DownloadManager.Query().setFilterById(downloadId)
        val progressHandler = Handler(Looper.getMainLooper())
        progressHandler.post(object : Runnable {
            override fun run() {
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                        progressDialog.dismiss()
                    } else {
                        val progressIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val totalIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val progress = cursor.getLong(progressIndex)
                        val total = cursor.getLong(totalIndex)
                        if (total >= 0) {
                            progressDialog.max = (total / (1024 * 1024)).toInt()
                            progressDialog.progress = (progress / (1024 * 1024)).toInt()
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

    private fun showDescSurah() {
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
            sbCurrent.min = 20
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