BLE 통신을 이용하여 적립 방식 비슷하게 구현해봄

처음에는 통신 이후 등록된 데이터를 가져오는 것으로 생각하였는데, 아무래도 토스에 클릭 시 데이터가 굉장히 빠르게 반응 된것을 보니

단순히 어떤 값과 내부에서 쓰는 토큰이나 사용자에 대한 데이터를 기준으로 해서 통신해서 포인트를 적립하는걸로 보인다.
그래서 대충 데이터가 나오면 클릭 이후 서버 통신 이후 적립 처리 하면 될것이라고 판단하였고, 실제로도 그런지는 모르겠지만 성공하였다.

그 후에는 문제는 이미 받은 사용자에 대한 중복 처리라던지 통신이 있지만 테스트 시점에서는 쓰지 않았다.

c내부 DB에 저장하여 마지막 날짜값을 기준으로 화면에 값 보여줄때 제외하고 보여주도록 하여 처리하였다
통신 부분은 rest API 통신 이다.  

이정도면 큰 틀에서 나머지 정리해서 서비스 코드로 변경하면 된다.


서비스 주소
```
class AdvertiserService : Service() {

    private val TAG = "bleTest"
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var handler: Handler? = null

    private val TIMEOUT: Long = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES)

    private var gattServer: BluetoothGattServer? = null
    private var ctfService: BluetoothGattService? = null

    private var manager: BluetoothManager? = null

    private val preparedWrites = HashMap<Int, ByteArray>()

    val namesReceived = MutableStateFlow(emptyList<String>())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        running = true
        initialize()
        startHandlingIncomingConnections()
        startAdvertising()
        setTimeout()
        super.onCreate()
    }

    override fun onDestroy() {
        running = false
        stopAdvertising()
        handler?.removeCallbacksAndMessages(null)
        stopHandlingIncomingConnections()
        //stopForeground(true)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun initialize() {
        if (bluetoothLeAdvertiser == null) {
            manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter: BluetoothAdapter = manager!!.adapter
            bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        }
    }

    private fun startAdvertising() {
        goForeground()
        Log.d(TAG, "Service: Starting Advertising")
        if (advertiseCallback == null) {
            val settings: AdvertiseSettings = buildAdvertiseSettings()
            val data: AdvertiseData = buildAdvertiseData()
            advertiseCallback = sampleAdvertiseCallback()
            bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
        }
    }

    private fun stopAdvertising() = bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        .also { bluetoothLeAdvertiser = null }

    private fun goForeground() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val nBuilder = run {
            val bleNotificationChannel = NotificationChannel(
                Utils.NOTIFICATION_NAME, "BLE",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(bleNotificationChannel)
            Notification.Builder(this, Utils.NOTIFICATION_NAME)
        }

        val notification = nBuilder.setContentTitle("주변에 친구를 찾는 중입니다.")
            .setContentText("함깨 켜기 시작")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            startForeground(FOREGROUND_NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }else{
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        }
    }

    private fun buildAdvertiseSettings() = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        .setTimeout(0).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM).build()

    
    var test01 = //대충 저장 되어 있는 어떤 값을 기준으로 해서 보내도록한다 

    private fun buildAdvertiseData() = AdvertiseData.Builder()
        .addServiceUuid(ScanFilterService_UUID)
        .setIncludeTxPowerLevel(false)
        .addServiceData(ScanFilterService_UUID, test01.toByteArray(Charsets.UTF_8))
        .setIncludeDeviceName(false).build()

    private fun sampleAdvertiseCallback() = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.d(TAG, "Advertising failed")
            broadcastFailureIntent(errorCode)
            stopSelf()
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising successfully started")
        }
    }

    private fun broadcastFailureIntent(errorCode: Int) {
        val failureIntent = Intent().setAction(ADVERTISING_FAILED).putExtra(
            BT_ADVERTISING_FAILED_EXTRA_CODE, errorCode
        )
        sendBroadcast(failureIntent)
    }

    private fun setTimeout() {
        handler = Handler(Looper.myLooper()!!)
        val runnable = Runnable {
            Log.d(
                TAG,
                "run: AdvertiserService has reached timeout of $TIMEOUT milliseconds, stopping advertising."
            )
            broadcastFailureIntent(ADVERTISING_TIMED_OUT)
        }
        handler?.postDelayed(runnable, TIMEOUT)
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    private fun startHandlingIncomingConnections() {
        gattServer = manager!!.openGattServer(this, object : BluetoothGattServerCallback() {
            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                super.onServiceAdded(status, service)
            }

            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

                Log.d(
                    TAG,
                    "읽어가는 값 onCharacteristicReadRequest"
                )

                val data = "a8read"
                var value = data.toByteArray()


                value = value.copyOfRange(offset, value.size)

                gattServer?.sendResponse(
                    device, requestId,
                    BluetoothGatt.GATT_SUCCESS, offset, value
                )


                /*gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    "note8Boom".encodeToByteArray()
                )*/
            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {
                super.onCharacteristicWriteRequest(
                    device,
                    requestId,
                    characteristic,
                    preparedWrite,
                    responseNeeded,
                    offset,
                    value
                )

                if (responseNeeded) {
                    Log.d(
                        TAG,
                        "onCharacteristicWriteRequest call "
                    )

                    if(preparedWrite) {
                        val bytes = preparedWrites.getOrDefault(requestId, byteArrayOf())
                        preparedWrites[requestId] = bytes.plus(value)
                    }
                    else {
                        namesReceived.update { it.plus(String(value)) }
                    }

                    Log.d(TAG, "받아온 값 : " + namesReceived.value[0])

                    gattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        byteArrayOf()
                    )
                }
            }

            override fun onExecuteWrite(
                device: BluetoothDevice?,
                requestId: Int,
                execute: Boolean
            ) {
                super.onExecuteWrite(device, requestId, execute)
                val bytes = preparedWrites.remove(requestId)
                if (execute && bytes != null) {
                    namesReceived.update { it.plus(String(bytes)) }
                }
            }
        })

        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val passwordCharacteristic = BluetoothGattCharacteristic(
            PASSWORD_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val nameCharacteristic = BluetoothGattCharacteristic(
            NAME_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        service.addCharacteristic(passwordCharacteristic)
        service.addCharacteristic(nameCharacteristic)
        gattServer?.addService(service)
        ctfService = service
    }

    companion object {
        var running: Boolean = false
    }

    private fun stopHandlingIncomingConnections() {
        ctfService?.let {
            gattServer?.removeService(it)
            ctfService = null
        }
    }
}


```

액티비티
```
class BlePointActivity : BaseActivity<ActivityBlePointBinding>({ ActivityBlePointBinding.inflate(it) }) {


    private lateinit var btAdvertisingFailureReceiver: BroadcastReceiver

    private var scanCallback: ScanCallback? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    lateinit var bluetoothAdapter: BluetoothAdapter

    private val TAG = "bleTest"
    private val SCAN_PERIOD_IN_MILLIS: Long = 300000 //5분 뒤 타임 아웃 처리 실제 사용시에는 반드시 넣을것 현재는 테스트라 고정했음

    var bluetoothGatt: BluetoothGatt? = null

    lateinit var scanViewModel: ScanViewModel


    override fun preLoad() {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            initRequestPermissionUPs {
                Log.d(TAG, "uptos")
                initAllSet()
            }
        } else {
            initRequestPermissionDownS {
                Log.d(TAG, "DownS")
                initAllSet()
            }
        }


        scanViewModel = ViewModelProvider(this)[ScanViewModel::class.java]

        binding.viewModel = scanViewModel

        binding.lifecycleOwner = this


        scanViewModel.itemList.observe(this){
            if (it.isNotEmpty()){
                for (i in it.indices){
                    checkAllView(it[i])
                }
            }

        }


        btAdvertisingFailureReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val errorCode = intent?.getIntExtra(BT_ADVERTISING_FAILED_EXTRA_CODE, INVALID_CODE)
                var errMsg = when (errorCode) {
                    AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> "ADVERTISE_FAILED_ALREADY_STARTED"
                    AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> "ADVERTISE_FAILED_DATA_TOO_LARGE"
                    AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "ADVERTISE_FAILED_FEATURE_UNSUPPORTED"
                    AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> "ADVERTISE_FAILED_INTERNAL_ERROR"
                    AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "Too many advertisers."
                    ADVERTISING_TIMED_OUT -> "Timed out."
                    else -> "Error unknown."
                }
                errMsg = "Start advertising failed: $errMsg"
                Toast.makeText(this@BlePointActivity, errMsg, Toast.LENGTH_LONG).show()
            }
        }

        binding.button.setOnClickListener {
            share()
        }
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "call onStop")
        stopAdvertising()
        stopScanning()
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "call onDestroy")
        stopAdvertising()
        stopScanning()
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    fun initAllSet() {
        if (bluetoothLeScanner == null) {
            val manager =
                this@BlePointActivity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = manager.adapter
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

            Log.d(TAG, "스캔 시작")
            //binding.testBackMain.playAnimation()
            binding.meIcon.playAnimation()
        }

        startAdvertising()

        if (scanCallback != null) {
            Log.d(TAG, "이미 스캔 시작 되었음")
        }
        scanCallback = SampleScanCallback()
        bluetoothLeScanner?.startScan(buildScanFilters(), buildScanSettings(), scanCallback)


        /*Handler(Looper.getMainLooper()).postDelayed({
            stopScanning()
            Log.d(TAG, "time out stopScanning")
        }, SCAN_PERIOD_IN_MILLIS)*/

    }
    private fun buildScanFilters(): List<ScanFilter> {
        val scanFilter = ScanFilter.Builder()
            //.setServiceUuid(ScanFilterService_UUID)
            .build()
        Log.d(TAG, "스캔 빌더 생성")
        return listOf(scanFilter)
    }

    private fun buildScanSettings() = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).setReportDelay(0).build()

    inner class SampleScanCallback : ScanCallback() {

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d(TAG, "onBatchScanResults size: ${results?.size}")
            results?.let {
                //scannerAdapter.setItems(it)
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            //scannerAdapter.addSingleItem(result)

            var bleResult = ""

            if (result == null || result.device == null) {
                Log.i(TAG, "데이터가 없는데 어떻게 호출 된거지?")
            } else {
                val data: String = result.scanRecord?.serviceUuids?.get(0).toString()

                if (data == SERVICE_UUID.toString()) run {

                    if (result.scanRecord?.deviceName.toString().isEmpty()) {
                        if (result.scanRecord!!.getServiceData(
                                result.scanRecord!!.serviceUuids[0]
                            ) != null
                        ) {
                            bleResult = String( //디바이스 패킷 데이터 호출
                                result.scanRecord!!.getServiceData(
                                    result.scanRecord!!.serviceUuids[0]
                                )!!, Charset.forName("UTF-8")
                            )
                            Log.d(TAG, "data else : " + bleResult)
                            scanViewModel.updateItem(EventBleData(bleResult))
                        }

                    } else {
                        bleResult = result.scanRecord?.deviceName.toString()
                        Log.d(TAG, "data else : " + bleResult)
                        scanViewModel.updateItem(EventBleData(bleResult))
                    }

                }

            }

        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(TAG, "onScanFailed: errorCode $errorCode")
        }
    }



    private fun createAdvertisingServiceIntent(): Intent =
        Intent(this@BlePointActivity, AdvertiserService::class.java)

    private fun startAdvertising() =
        this@BlePointActivity.startService(createAdvertisingServiceIntent())

    private fun stopAdvertising() {
        this@BlePointActivity.stopService(createAdvertisingServiceIntent())
    }

    private fun stopScanning() {
        Log.d(TAG, "stopScanning")
        bluetoothLeScanner?.stopScan(scanCallback)
        scanCallback = null
    }


    fun checkAllView(eventBleData: EventBleData) {
        for (i in 0..3){
            when (i) {
                0 -> {
                    if (!binding.scon00.itemLayout.isVisible){
                        //todo 여기서 화면 구성
                        binding.scon00.root.visibility = View.VISIBLE
                        binding.scon00.itemTitle.text = eventBleData.title.toString()
                        binding.scon00.itemIcon.progress = 0f
                        binding.scon00.itemLayout.setOnClickListener {

                            binding.scon00.itemIcon.addAnimatorListener(object :
                                Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {

                                }

                                override fun onAnimationEnd(animation: Animator) {
                                    binding.scon00.root.visibility = View.GONE
                                }

                                override fun onAnimationCancel(animation: Animator) {

                                }

                                override fun onAnimationRepeat(animation: Animator) {

                                }

                            })
                            binding.scon00.itemIcon.playAnimation()
                            eventCall(eventBleData)
                        }
                    }
                    break
                }
                1 -> {
                    if (!binding.scon01.itemLayout.isVisible){
                        //todo 여기서 화면 구성
                        binding.scon01.root.visibility = View.VISIBLE
                        binding.scon01.itemTitle.text = eventBleData.title.toString()
                        binding.scon01.itemIcon.progress = 0f
                        binding.scon01.itemLayout.setOnClickListener {

                            binding.scon01.itemIcon.addAnimatorListener(object :
                                Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {

                                }

                                override fun onAnimationEnd(animation: Animator) {
                                    binding.scon01.root.visibility = View.GONE
                                }

                                override fun onAnimationCancel(animation: Animator) {

                                }

                                override fun onAnimationRepeat(animation: Animator) {

                                }

                            })
                            binding.scon01.itemIcon.playAnimation()
                            eventCall(eventBleData)
                        }
                    }
                    break
                }
                2 -> {
                    if (!binding.scon02.itemLayout.isVisible){
                        binding.scon02.root.visibility = View.VISIBLE
                        binding.scon02.itemTitle.text = eventBleData.title.toString()
                        binding.scon02.itemIcon.progress = 0f
                        binding.scon02.itemLayout.setOnClickListener {

                            binding.scon02.itemIcon.addAnimatorListener(object :
                                Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {

                                }

                                override fun onAnimationEnd(animation: Animator) {
                                    binding.scon02.root.visibility = View.GONE
                                }

                                override fun onAnimationCancel(animation: Animator) {

                                }

                                override fun onAnimationRepeat(animation: Animator) {

                                }

                            })
                            binding.scon02.itemIcon.playAnimation()
                            eventCall(eventBleData)
                        }
                    }
                    break
                }
                3 -> {
                    if (!binding.scon03.itemLayout.isVisible){
                        binding.scon03.root.visibility = View.VISIBLE
                        binding.scon03.itemTitle.text = eventBleData.title.toString()
                        binding.scon03.itemIcon.progress = 0f
                        binding.scon03.itemLayout.setOnClickListener {

                            binding.scon03.itemIcon.addAnimatorListener(object :
                                Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {

                                }

                                override fun onAnimationEnd(animation: Animator) {
                                    binding.scon03.root.visibility = View.GONE
                                }

                                override fun onAnimationCancel(animation: Animator) {

                                }

                                override fun onAnimationRepeat(animation: Animator) {

                                }

                            })
                            binding.scon03.itemIcon.playAnimation()
                            eventCall(eventBleData)
                        }
                    }
                    break
                }

            }

        }
    }

    fun initRequestPermissionUPs(logic: () -> Unit) {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    logic()
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Log.d(TAG, "initRequestPermissionUPs 거절")
                    Log.d(TAG, "list : " + deniedPermissions.toString())
                }
            })

            //android.Manifest.permission.BLUETOOTH,
            //                android.Manifest.permission.BLUETOOTH_ADMIN,
            .setPermissions(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                android.Manifest.permission.POST_NOTIFICATIONS,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.FOREGROUND_SERVICE
            )
            .check()
    }

    fun initRequestPermissionDownS(logic: () -> Unit) {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    logic()
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Log.d(TAG, "initRequestPermissionDownS 거절")
                    Log.d(TAG, "list : " + deniedPermissions.toString())
                }
            })
            .setPermissions(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.FOREGROUND_SERVICE
            )
            .check()
    }

    private fun eventCall(insertData: EventBleData) {
        /*val dateFormatter: DateFormat = SimpleDateFormat("yyyyMMdd")
        dateFormatter.isLenient = false
        val today = Date()
        val s = dateFormatter.format(today)
        val dateToLong = s.toLong()
        Log.d(TAG, "today : " + s.toString())
        Log.d(TAG, "today Long : " + dateToLong.toString())*/

        scanViewModel.removeItem(insertData)

        Toast.makeText(
            this@BlePointActivity,
            "서버에 데이터 전송하고 100원 적립",
            Toast.LENGTH_LONG
        ).show()

    }

    fun share() {
        Log.d("inTestCase", "share")
        val commonLinkDialog =
            CommonLinkDialog(
                this@BlePointActivity,
                "data"
            )
        commonLinkDialog.show(
            supportFragmentManager,
            commonLinkDialog.tag
        )

    }

}

```

ViewModel 데이터

```
class ScanViewModel : ViewModel() {

    private val list = mutableListOf<EventBleData>()
    private val _itemList = MutableLiveData<List<EventBleData>>()
    val itemList: LiveData<List<EventBleData>> = _itemList


    init {
        _itemList.value = list
    }

    fun removeItem(item: EventBleData) {
        list.remove(item)
        _itemList.value = list
    }

    fun updateItem(item: EventBleData){
        if (list.count() < 4){
            if (!list.contains(item)){
                list.add(item)
                _itemList.value = list
            }
        }
    }

}

```

Room 이용한 DB

```

@Entity(tableName = "recent_ble_table") //테이블명
data class BleData(
    var pairDeviceID: String,
    var pairDate: Long,
    var userID: String
){
    @PrimaryKey(autoGenerate = true)
    var index: Int = 0
}

@Database(entities = [BleData::class], version = 1) //최근 블루투스 통신
@TypeConverters(Converters::class)
abstract class BleDataBase : RoomDatabase() {

    abstract fun recentBleDao(): RecentBleDao

    companion object {
        @Synchronized
        fun getInstance(context: Context): BleDataBase? {
            var instance: BleDataBase? = null
            if (instance == null) {
                synchronized(BleDataBase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BleDataBase::class.java,
                        "recent-ble-database"
                    ).build()
                }
            }
            return instance
        }
    }
}


@Dao
interface RecentBleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT) //충돌이 발생할 경우 처리 중단
    fun insert(bleData: BleData)  // 데이터 생성 입력

    @Query("SELECT * FROM recent_ble_table ORDER BY pairDate ASC")
    fun getAll(): List<BleData> //전체 데이터 가져오기

    @Query("DELETE FROM recent_ble_table WHERE pairDate == :initDate")
    fun deleteDate(initDate : Long)//현 시간 보다 1일전 데이터 삭제 1번 호출*/

    @Query("SELECT * FROM recent_ble_table WHERE userID == :userid AND pairDeviceID == :bleID AND  pairDate == :initDate")
    fun checkData(userid: String, bleID: String, initDate : Long): List<BleData> //리스트 데이터

}


```



