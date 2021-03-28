# MLRecognition
Face and text recognition
 

### Init SDK 
#### In onCreate(..) method of Application or Activity class
```kotlin

private lateinit var scanSDK: ScanSDK
override fun onCreate(savedInstanceState: Bundle?) {
    ...
    scanSDK = ScanSDK.init(this)
}
```
#### Via Dagger method module
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
