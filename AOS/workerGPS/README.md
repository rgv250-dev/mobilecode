워크 매니저 예시

워크 매니저를 이용하여 사용자의 GPS 정보를 서버로 전달

GPS 권한이 안됨

예시로는 카시오와치앱의 블루투스로 시계의 시간 조정하는 작업같은 지속적이고 보장된 작업에 사용하는것이 좋음

PeriodicWorkRequest를 생성할 때 설정할 수 있는 최소 간격은 15분입니다.

```
class TestWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val mContext = context
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(mContext)
    }

    private val logTag = "workerTest"

    override suspend fun doWork(): Result {
        if (!hasLocationPermission()) {
            Log.e(logTag, "위치 권한이 없습니다.")
            return Result.failure()
        }

        return try {
            processLocation()
            Result.success()
        } catch (e: Exception) {
            Log.e(logTag, "doWork 예외 발생: ${e.message}", e)
            Result.failure()
        }
    }

    /**
     * 위치 권한 확인
     */
    private fun hasLocationPermission(): Boolean {
        val fineLocationPermission = ActivityCompat.checkSelfPermission(
            mContext, android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ActivityCompat.checkSelfPermission(
            mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 위치 데이터를 처리하고 서버로 전송
     */
    private suspend fun processLocation() {
        try {
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                val data = createLocationData(lastLocation.latitude, lastLocation.longitude)
                if (Utils.isOnline(mContext)) {
                    sendDataToServer(data)
                } else {
                    Log.e(logTag, "네트워크가 연결되지 않았습니다.")
                }
            } else {
                Log.e(logTag, "마지막 위치를 가져올 수 없습니다.")
            }
        } catch (e: SecurityException) {
            Log.e(logTag, "위치 보안 예외 발생: ${e.message}")
        }
    }

    /**
     * 위치 데이터를 생성
     */
    private fun createLocationData(lat: Double, lng: Double): JsonObject {
        return JsonObject().apply {
            addProperty("lat", lat.toString())
            addProperty("lng", lng.toString())
            addProperty("userData", "testUser")
        }
    }

    /**
     * 서버로 위치 데이터를 전송
     * 단순 전달용 어떠한 작업을 진행하지 않음
     */
    private suspend fun sendDataToServer(data: JsonObject) {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e("KokkanWorker", "데이터 전송 중 오류 발생: ${throwable.message}", throwable)
        }

        withContext(Dispatchers.IO + exceptionHandler) {
            try {
                val response = TestWorkerRestUtil.api.sendInfoData(data)
                if (response.isSuccessful) {
                    Log.d(logTag, "데이터 전송 성공")
                } else {
                    Log.e(logTag, "데이터 전송 실패: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(logTag, "서버 전송 실패: ${e.message}", e)
            }
        }
    }
}


```

기기가 충전중이지 않아도 된다 
실행 시간을 설정하고 만약 이미 등록되어 있다면 업데이트 처리 하도록 한다.
PeriodicWorkRequest를 생성할 때 설정할 수 있는 최소 간격은 15분
```
//권한 체크 하고 생략 
val constraints = Constraints.Builder().setRequiresCharging(false).build()
                val periodicWorkRequest =
                    PeriodicWorkRequestBuilder<TestWorker>("실행주기 시간 설정", TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                WorkManager.getInstance(application).enqueueUniquePeriodicWork(
                    "TestWorker", ExistingPeriodicWorkPolicy.UPDATE, periodicWorkRequest
)
```
