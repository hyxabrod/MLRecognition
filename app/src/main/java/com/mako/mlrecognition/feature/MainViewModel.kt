package com.mako.mlrecognition.feature

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mako.mlrecognition.SingleLiveEvent
import com.mako.scansdk.ScanSDK
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val scanSDK: ScanSDK
): ViewModel() {
    private val _textVisibility = MutableLiveData<Boolean>()
    val textVisibility: LiveData<Boolean> = _textVisibility

    private val _text = MutableLiveData<TextState>()
    val text: LiveData<TextState> = _text

    private val _faceVisibility = MutableLiveData<Boolean>()
    val faceVisibility: LiveData<Boolean> = _faceVisibility

    private val _image = MutableLiveData<String>()
    val image: LiveData<String> = _image

    private val _navigation = SingleLiveEvent<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    fun onStartTextScanClicked() {
        _navigation.postValue(Navigation.TextRecognition)
    }

    fun onStartFaceScanClicked() {
        _navigation.postValue(Navigation.FaceRecognition)
    }

    fun onHandleFaceScanResult(data: String?) {
        _faceVisibility.postValue(data != null)
        _textVisibility.postValue(data == null)

        if (data.isNullOrBlank()) {
            _text.postValue(TextState.Error)
        } else {
            _image.postValue(data!!)
        }
    }

    fun onHandleTextScanResult(data: String?) {
        _faceVisibility.postValue(data == null)
        _textVisibility.postValue(data != null)

        if (data.isNullOrBlank()) {
            _text.postValue(TextState.Error)
        } else {
            _text.postValue(TextState.Message(data!!))
        }
    }

    fun onDestroy() {
        scanSDK.clearCachedData()
    }

    sealed class TextState {
        object Error : TextState()
        data class Message(val value: String) : TextState()
    }

    sealed class Navigation {
        object FaceRecognition : Navigation()
        object TextRecognition : Navigation()
    }
}