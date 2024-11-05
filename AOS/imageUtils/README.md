워크매니저를 이용한 백그라운드 GPS 연동

물론 당연하게도 권한을 받지 않고는 이걸 쓸 수 있는 방법은 없다.

여러가지 백그라운드 GPS를 받는 방법은 있지만 개인적으로는 매 시간, 매초 매분으로 받는거라면 포그라운드를 이용하는게 맞다.

못해도 몇분 마다 돈다고 가정하면 워크매니저를 통해서 GPS 정보를 보내는게 효율적이다 판단한다.

사실 더 많은 방법이 있지만 그냥 보내던걸로 끝낼거면 이정도가 제일 좋더라.

```

class KokkanWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    var mContext = context

    private val mFusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(mContext)
    }

    override suspend fun doWork(): Result {

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure()
        }
        checkDistance()

        return Result.success()
    }

    private fun checkDistance() {
        try {
            //마지막 위치를 가져오는데에 성공한다면
            mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { aLocation ->
                        val fromLat = aLocation.latitude
                        val fromLng = aLocation.longitude
                        val userData = 토큰 가져오는 곳

                        val data = JsonObject()
                        data.addProperty("lat", fromLat.toString())
                        data.addProperty("lng", fromLng.toString())
                        data.addProperty("userData", userData)
                        //위도 경도 유저 유저 토큰을 보내서 확인함

                        val exceptionHandler =
                            CoroutineExceptionHandler { coroutineContext, throwable ->
                                Log.e("postData", "throwable : " + throwable.message)
                                when (throwable) {
                                    is SocketException -> {
                                        Log.d("postData", "erorr SocketException : ")
                                    }

                                    is HttpException -> {
                                        Log.d("postData", "erorr HttpException : ")
                                    }

                                    is UnknownHostException -> {
                                        Log.d("postData", "erorr UnknownHostException : ")
                                    }

                                    else -> {
                                        Log.d("postData", "erorr throwable : ${throwable.message}")
                                    }
                                }
                            }

                        if (Utils.isOnline(mContext) && userPhone.isNotEmpty()) {
                            CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                                val postmData = restUtil.api.postGPSData(data)
                                if (postData.isSuccessful) {
                                    Log.d("postData", "post")
                                } else {
                                    Log.d("postData", "erorr : " + postFcmData.message().toString())
                                }
                            }
                        } else {
                            Log.d("postData", "do worker else??")
                        }

                    }
                } else {
                    Log.d("postData", "task erorr")
                }
            }
        } catch (err: SecurityException) {
            Log.d("postData", "SecurityException")
        }
    }
}


```
메소드를 통해 워크매니저를 등록

```

   if (Utils.permissionCheckBack(this@MainActivity)) {
                val constraints = Constraints.Builder().setRequiresCharging(false).build()
                val periodicWorkRequest =
                    PeriodicWorkRequestBuilder<wokerGPS>('최소시간', TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                WorkManager.getInstance(application).enqueueUniquePeriodicWork(
                    "wokerGPS", ExistingPeriodicWorkPolicy.UPDATE, periodicWorkRequest
                )
            }


```



```


```