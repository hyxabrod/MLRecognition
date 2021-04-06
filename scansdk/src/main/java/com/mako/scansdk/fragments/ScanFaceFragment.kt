package com.mako.scansdk.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.*
import com.mako.scansdk.R
import com.mako.scansdk.camera.FacePositionListener
import com.mako.scansdk.camera.FaceScanManager
import com.mako.scansdk.utils.FileUtil
import com.mako.scansdk.utils.rotateFlipImage
import kotlinx.android.synthetic.main.scan_face_scan_fragment.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


const val SCAN_FACE_RESULT_KEY = "scan_face_result_key"
const val SCAN_FACE_FRAGMENT_RESULT = "scan_face_fragment_result"
private const val REQUEST_CODE_PERMISSIONS = 2041

@FlowPreview
class ScanFaceFragment : Fragment(), LifecycleObserver, FacePositionListener {

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS
        )

        fun newInstance() = ScanFaceFragment()
    }

    private lateinit var cameraManagerManager: FaceScanManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.scan_face_scan_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        cameraManagerManager = FaceScanManager(
            requireActivity(),
            pv_preview_face,
            requireActivity(),
            overlay_face
        ).also {
            it.setCorrectFacePositionListener(this)
            it.setCameraErrorCallback {
                if (isAdded) {
                    Toast.makeText(requireActivity(), "No cameras found", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            }
        }

        lifecycle.addObserver(cameraManagerManager)

        setupListeners()
        checkPermissions()
    }

    private fun setupListeners() {
        btn_capture_face.setOnClickListener {
            pb_capture_face.isVisible = true
            scanAndAnalyze()
        }
    }

    private fun scanAndAnalyze() {
        cameraManagerManager.getResult(block = {
            lifecycleScope.launch {
                FileUtil.saveBitmapToStorage(
                    requireContext(),
                    it.rotateFlipImage()
                ).collect {
                    delay(500)
                    when (it) {
                        is FileUtil.SaveToFileResult.Error -> {
                            handleError(it.e.message)
                        }
                        is FileUtil.SaveToFileResult.FilePath -> {
                            setFragmentResult(
                                SCAN_FACE_RESULT_KEY,
                                bundleOf(Pair(SCAN_FACE_FRAGMENT_RESULT, it.path))
                            )
                        }
                    }
                }
            }
        }, ::handleError)
    }

    private fun handleError(message: String?) {
        setFragmentResult(
            SCAN_FACE_RESULT_KEY,
            bundleOf(Pair(SCAN_FACE_FRAGMENT_RESULT, message))
        )
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

    override fun isPositionCorrect(correct: Boolean) {
        btn_capture_face.isEnabled = correct
    }
}




