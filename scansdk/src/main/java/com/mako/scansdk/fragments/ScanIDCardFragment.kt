package com.mako.scansdk.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.*
import com.mako.scansdk.R
import com.mako.scansdk.camera.TextScanManager
import kotlinx.android.synthetic.main.scan_id_card_fragment.*

const val SCAN_ID_FRAGMENT_RESULT_KEY = "scan_id_fragment_result_key"
const val SCAN_ID_FRAGMENT_RESULT = "scan_id_fragment_result"
private const val REQUEST_CODE_PERMISSIONS = 2041

class ScanIDCardFragment : Fragment(), LifecycleObserver {

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
        fun newInstance() = ScanIDCardFragment()
    }

    private var cameraHeight = 0
    private var cameraWidth: Int = 0
    private var xOffset: Int = 0
    private var yOffset: Int = 0
    private var boxWidth: Int = 0
    private var boxHeight: Int = 0

    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private lateinit var holder: SurfaceHolder
    private lateinit var cardRect: Rect

    private lateinit var cameraManagerManager: TextScanManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.scan_id_card_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        cameraManagerManager = TextScanManager(requireActivity(), pv_preview, requireActivity())

        lifecycle.addObserver(cameraManagerManager)
        setupViews()
        setupListeners()
        checkPermissions()
    }

    private fun setupViews() {
        overlay.setZOrderOnTop(true)
        holder = overlay.holder
        holder.setFormat(PixelFormat.TRANSPARENT)
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
                drawRect(Color.parseColor("#FFFFFF"))
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
            }
        })
    }

    private fun setupListeners() {
        btn_capture.setOnClickListener {
            pb_capture.isVisible = true
            scanAndAnalyze()
        }
    }

    private fun scanAndAnalyze() {
        cameraManagerManager.scanAndAnalyzeText(block = {
            setFragmentResult(
                SCAN_ID_FRAGMENT_RESULT_KEY,
                bundleOf(Pair(SCAN_ID_FRAGMENT_RESULT, it))
            )
        }, error = {
            setFragmentResult(
                SCAN_ID_FRAGMENT_RESULT_KEY,
                bundleOf(Pair(SCAN_ID_FRAGMENT_RESULT, null))
            )
        })
    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            cameraManagerManager.startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun drawRect(color: Int) {
        val height: Int = pv_preview.height
        val width: Int = pv_preview.width
        cameraHeight = height
        cameraWidth = width
        val left: Float
        val right: Float
        val top: Float
        val bottom: Float
        canvas = holder.lockCanvas()
        val cardBoxPadding = width * 0.1
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)

        paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = 5f

        left = cardBoxPadding.toFloat()
        right = (width - cardBoxPadding).toFloat()

        val cardWidth: Float = right - left
        //5.4 x 8.5 - ID card proportions
        val cardHeight: Float = cardWidth * 5.4f / 8.5f

        top = height / 2 - cardHeight / 2
        bottom = height / 2 + cardHeight / 2

        cardRect = Rect(
            left.toInt(), top.toInt(), right.toInt(), bottom.toInt()
        )

        xOffset = left.toInt()
        yOffset = top.toInt()

        boxHeight = (bottom - top).toInt()
        boxWidth = (right - left).toInt()
        canvas.drawRect(left, top, right, bottom, paint)
        holder.unlockCanvasAndPost(canvas)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraManagerManager.startCamera()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
        }
    }
}




