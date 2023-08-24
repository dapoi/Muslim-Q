package com.prodev.muslimq.presentation.view.others.bookmark

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.DialogDeleteAllBinding
import com.prodev.muslimq.databinding.DialogInfoSurahBinding
import com.prodev.muslimq.databinding.FragmentBookmarkBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.adapter.QuranBookmarkAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.view.quran.QuranDetailFragment
import com.prodev.muslimq.presentation.viewmodel.BookmarkViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment : BaseFragment<FragmentBookmarkBinding>(FragmentBookmarkBinding::inflate) {

    private lateinit var quranBookmarkAdapter: QuranBookmarkAdapter

    private val quranBookmarkViewModel: BookmarkViewModel by viewModels()
    private val curvedDialog by lazy {
        AlertDialog.Builder(requireContext(), R.style.CurvedDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initViewModel()

        binding.apply {
            ivBack.setOnClickListener { findNavController().navigateUp() }

            ivMore.setOnClickListener { showMenuOption() }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showMenuOption() {
        val popupMenu = PopupMenu(requireContext(), binding.ivMore)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }
        popupMenu.menuInflater.inflate(R.menu.menu_bookmark, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_info -> {
                    DialogInfoSurahBinding.inflate(layoutInflater).apply {
                        tvInfoTitle.text = "Info"
                        tvInfoMessage.text =
                            "Anda dapat menghapus tiap surah dengan cara geser kiri"
                        with(curvedDialog.create()) {
                            setView(root)
                            tvInfoClose.setOnClickListener { dismiss() }
                            show()
                        }
                    }
                    true
                }

                R.id.action_delete_all -> {
                    DialogDeleteAllBinding.inflate(layoutInflater).apply {
                        with(curvedDialog.create()) {
                            setView(root)
                            tvConfirm.setOnClickListener {
                                quranBookmarkViewModel.deleteAllBookmark()
                                dismiss()

                                (activity as MainActivity).customSnackbar(
                                    state = true,
                                    context = requireContext(),
                                    view = binding.root,
                                    message = "Berhasil menghapus semua surah"
                                )
                            }
                            tvCancel.setOnClickListener { dismiss() }
                            show()
                        }
                    }
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun initAdapter() {
        quranBookmarkAdapter = QuranBookmarkAdapter(
            onItemClick = { surah ->
                findNavController().navigate(R.id.action_quranBookmarkFragment_to_quranDetailFragment,
                    Bundle().apply {
                        putString(QuranDetailFragment.SURAH_NAME, surah.nama)
                        putInt(QuranDetailFragment.SURAH_NUMBER, surah.surahId)
                        putString(QuranDetailFragment.SURAH_DESC, surah.deskripsi)
                    }
                )
            }
        )

        val background = ColorDrawable()
        val backgroundColor = ContextCompat.getColor(requireContext(), R.color.red)
        val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
        deleteIcon!!.setTint(ContextCompat.getColor(requireContext(), R.color.white_always))
        val intrinsicWidth = deleteIcon.intrinsicWidth
        val intrinsicHeight = deleteIcon.intrinsicHeight

        binding.rvSurah.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = quranBookmarkAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.adapterPosition
                val surah = quranBookmarkAdapter.currentList[position]
                quranBookmarkViewModel.deleteBookmark(surah)

                (activity as MainActivity).customSnackbar(
                    state = true,
                    context = requireContext(),
                    view = binding.root,
                    message = "Berhasil menghapus surah ${surah.namaLatin}"
                )
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val isCanceled = dX == 0f && !isCurrentlyActive

                if (isCanceled) {
                    clearCanvas(
                        c,
                        itemView.right + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat(),
                        clearPaint
                    )
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        false
                    )
                    return
                }

                // Draw the red delete background
                background.color = backgroundColor
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)

                // Calculate position of delete icon
                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + intrinsicHeight

                // Draw the delete icon
                deleteIcon.setBounds(
                    deleteIconLeft,
                    deleteIconTop,
                    deleteIconRight,
                    deleteIconBottom
                )
                deleteIcon.draw(c)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvSurah)
    }

    private fun clearCanvas(
        c: Canvas?,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        clearPaint: Paint
    ) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }

    private fun initViewModel() {
        quranBookmarkViewModel.getBookmark().observe(viewLifecycleOwner) { quran ->
            binding.apply {
                if (quran.isNullOrEmpty()) {
                    clNegativeCase.visibility = View.VISIBLE
                    rvSurah.visibility = View.GONE
                    ivMore.visibility = View.GONE
                } else {
                    clNegativeCase.visibility = View.GONE
                    rvSurah.visibility = View.VISIBLE
                    ivMore.visibility = View.VISIBLE
                    quranBookmarkAdapter.submitList(quran)
                }
            }
        }
    }
}