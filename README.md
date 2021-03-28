# MLRecognition
Face and text recognition SDK
Provides fragments UI and ML recognition (with returning results)
 

### SDK initialization
#### In onCreate(..) method of Application or Activity class
```kotlin

private lateinit var scanSDK: ScanSDK
override fun onCreate(savedInstanceState: Bundle?) {
    ...
    scanSDK = ScanSDK.init(this)
}
```
#### ...or via Dagger method module
```kotlin

@Module
class ScanModule {
    @Provides
    @Singleton
    fun provideScanSDK(context: Context): ScanSDK {
        return ScanSDK.init(context)
    }
}
```

#### Clear cached data on view  destroyed
```kotlin

override fun onDestroy() {
        scanSDK.clearCachedData()
        super.onDestroy()
    }
```

#### Create face recognition fragment
```kotlin
...
   fragment = ScanFaceFragment.newInstance()
...
```

#### Create ID recognition fragment
```kotlin
...
   fragment = ScanIDCardFragment.newInstance()
...
```

#### Subscribe for ID text recognition results
##### Returns either List of the recognized strings, or null is case of an error
```kotlin
...
   fragment.setFragmentResultListener(
                    SCAN_ID_FRAGMENT_RESULT_KEY,
                    this@MainActivity
                ) { _, bundle ->
                        ...
                        onHandleFaceScanResult(bundle.getString(SCAN_ID_FRAGMENT_RESULT))
                        ...
                }
...
```


#### Subscribe for face recognition results
##### Returns either path to image file, or null is case of an error
```kotlin
...
   fragment.setFragmentResultListener(
                    SCAN_FACE_RESULT_KEY,
                    this@MainActivity
                ) { _, bundle ->
                        ...
                        onHandleFaceScanResult(bundle.getString(SCAN_FACE_FRAGMENT_RESULT))
                        ...
                }
...
```
