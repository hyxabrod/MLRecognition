package com.mako.mlrecognition.feature

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mako.mlrecognition.R
import com.mako.mlrecognition.feature.MainViewModel.*
import com.mako.scansdk.*
import com.mako.scansdk.fragments.*
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
class MainActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val scanTextFragmentTag by lazy { ScanIDCardFragment::class.java.canonicalName }
    private val scanFaceFragmentTag by lazy { ScanFaceFragment::class.java.canonicalName }

    private val viewModel: MainViewModel by viewModels() {
        viewModelFactory
    }

    @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupListeners()

        viewModel.apply {
            observe(navigation, ::onNavigation)
            observe(textVisibility, ::onTextVisibility)
            observe(faceVisibility, ::onFaceVisibility)
            observe(text, ::onText)
            observe(image, ::onImage)
        }
    }

    private fun onImage(path: String?) {
        path ?: return

        Glide.with(this@MainActivity)
            .load(path)
            .centerInside()
            .format(DecodeFormat.PREFER_RGB_565)
            .disallowHardwareConfig()
            .into(iv_scanned_face)
    }

    private fun onText(textState: TextState?) {
        textState ?: return
        when (textState) {
            TextState.Error -> tv_scanned_text.text = getString(R.string.scan_failed)
            is TextState.Message -> tv_scanned_text.text = textState.value
        }
    }

    private fun onFaceVisibility(isVisible: Boolean?) {
        iv_scanned_face.isVisible = isVisible == true
    }

    private fun onTextVisibility(isVisible: Boolean?) {
        tv_scanned_text.isVisible = isVisible == true
    }

    private fun onNavigation(navigation: Navigation?) {
        navigation?.let {
            when (it) {
                Navigation.FaceRecognition -> showScanFaceFragment()
                Navigation.TextRecognition -> showScanTextFragment()
            }
        }
    }

    private fun setupListeners() {
        btn_start_text_scanner.setOnClickListener {
            viewModel.onStartTextScanClicked()
        }
        btn_start_face_scanner.setOnClickListener {
            viewModel.onStartFaceScanClicked()
        }
    }

    private fun showScanTextFragment() {
        currentFragment = getScanIdFragment()
        currentFragment?.let { fragment ->
            if (fragment.isAdded) return
            supportFragmentManager.apply {
                with(beginTransaction()) {
                    add(android.R.id.content, fragment, scanTextFragmentTag)
                    commit()
                }
                setFragmentResultListener(
                    SCAN_ID_FRAGMENT_RESULT_KEY,
                    this@MainActivity
                ) { _, bundle ->
                    viewModel.onHandleTextScanResult(bundle.getString(SCAN_ID_FRAGMENT_RESULT))
                    removeFragment(fragment)
                }
            }
        }
    }

    private fun showScanFaceFragment() {
        iv_scanned_face.setImageResource(0)
        currentFragment = getScanFaceFragment()
        currentFragment?.let { fragment ->
            if (fragment.isAdded) return
            supportFragmentManager.apply {
                with(beginTransaction()) {
                    add(android.R.id.content, fragment, scanFaceFragmentTag)
                    commit()
                }
                setFragmentResultListener(
                    SCAN_TEXT_RESULT_KEY,
                    this@MainActivity
                ) { _, bundle ->
                    lifecycleScope.launchWhenResumed {
                        viewModel.onHandleFaceScanResult(bundle.getString(SCAN_FACE_FRAGMENT_RESULT))
                        removeFragment(fragment)
                    }
                }
            }
        }
    }

    private fun removeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .remove(fragment)
            .commitAllowingStateLoss()
    }

    private var currentFragment: Fragment? = null

    private fun getScanIdFragment() =
        supportFragmentManager.findFragmentByTag(scanTextFragmentTag)
            ?: ScanIDCardFragment.newInstance()

    private fun getScanFaceFragment() =
        supportFragmentManager.findFragmentByTag(scanFaceFragmentTag)
            ?: ScanFaceFragment.newInstance()

    override fun onBackPressed() {
        if (currentFragment?.isAdded == true) {
            currentFragment?.let {
                removeFragment(it)
            } ?: onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        //Glide.with(this).clear(iv_scanned_face)
        viewModel.onDestroy()
        super.onDestroy()
    }
}

fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T?) -> Unit) {
    liveData.observe(this, Observer(body))
}

