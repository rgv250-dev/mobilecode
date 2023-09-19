object Utils {
    //가끔 자주 쓰는 유틸 모음

    val emailValidation =
        "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

    /**
     * px크기를 dp로 교체
     * @param context 현재 화면
     * @param dps  적용할 사이즈
     * @return Int px값
     */
    fun pxFromDp(context: Context, dps: Int): Int {
        return (context.resources.displayMetrics.density * dps).roundToInt()
    }


    fun getStringSizeImageLengthFile(size: Long): String {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb

        if (size < sizeMb) {
            Log.d(
                "getStringSizeImageLengthFile", "size < sizeMb : " + df.format(size / sizeMb)
                    .toString() + " Mb"
            )

            return "true"
        } else if (size < sizeGb) {
            val sizeMB = size / sizeMb
            Log.d(
                "getStringSizeImageLengthFile", "Image size : " + df.format(size / sizeMb)
                    .toString() + " Mb"
            )
            return if (sizeMB < 1f) {
                "true"
            } else {
                ""
            }


        } else {
            return ""
        }

        /*if (size < sizeMb) return df.format(size / sizeKb)
            .toString() + " Kb" else if (size < sizeGb) return df.format(size / sizeMb)
            .toString() + " Mb" else if (size < sizeTerra) return df.format(size / sizeGb)
            .toString() + " Gb"*/
    }

    fun getStringSizeLengthFile(size: Long): String {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        if (size < sizeMb) return df.format(size / sizeKb)
            .toString() + " Kb" else if (size < sizeGb) return df.format(size / sizeMb)
            .toString() + " Mb" else if (size < sizeTerra) return df.format(size / sizeGb)
            .toString() + " Gb"
        return ""
    }

    /**
     * 비디오 사이즈 확인하는 유틸
     * @param size  Long으로 구성된 파일 사이즈
     * @return String 값으로 사이즈 표기
     */
    fun getStringSizeVideoFile(size: Long): String {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb

        if (size < sizeMb) {
            return "true"
        } else if (size < sizeGb) {
            val sizeMB = size / sizeMb

            Log.d(
                "getStringSizeImageLengthFile", "Video size : " + df.format(size / sizeMb)
                    .toString() + " Mb"
            )

            if (sizeMB < 50f) {
                return "true"
            } else {
                return ""
            }
        } else {
            return ""
        }

    }


    fun checkEmail(context: Context, editText: TextInputEditText): Boolean {
        val email = editText.text.toString().trim() //공백제거
        val p = Pattern.matches(emailValidation, email) //
        return if (p) {
            //이메일 형태가 정상일 경우
            editText.setTextColor(R.color.black.toInt())
            editText.background =
                context.resources.getDrawable(R.drawable.edit_backgound_selector, null)
            true
        } else {
            editText.error = "이메일 형식을 확인해주세요."
            editText.background =
                context.resources.getDrawable(R.drawable.back_ground_f96552_round_19_line, null)
            false
        }
    }

    /**
     * 앱 인터넷 체크
     * @param context 사용 되는 화면 Context
     * @return Boolean true or false
     */
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    /**
     * 로컬 타임 사용하는 dateFormat 리턴하는 함수
     * @param pattern
     * @param locale
     * @return dateFormat
     */
    fun getDefaultDateFormat(pattern: String?, locale: Locale): SimpleDateFormat {
        val dateFormat = SimpleDateFormat(pattern, locale)
        if (locale === Locale.KOREA) {
            dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        return dateFormat
    }

    /**
     * 숫자, 숫자 으로 구성된 String값을 List<Int> 타입으로 돌려주는 유틸
     * @param data ,으로 구성된 String
     * @return List<Int>
     */
    fun stringToIntList(data: String): List<Int> =
        data.split(",").filter { it.toIntOrNull() != null }
            .map { it.toInt() }


    /**
     * device 모델명 가져오기
     * @return MODEL
     */
    fun getDeviceModel(): String? {
        return Build.MODEL
    }

    /**
     * device Android OS 버전 가져오기
     * @return VERSION
     */
    fun getDeviceOs(): String? {
        return Build.VERSION.RELEASE
    }


    //스크린 사이즈 돌려주는 유틸
    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    fun isActivityAvailable(activity: Activity): Boolean {
        return !activity.isFinishing && !activity.isDestroyed
    }

    /**
     * UTC String 데이터를 받아
     * @return String yyyy-MM-dd String으로 돌려줌
     */
    fun formatDateTimeDateDot(inputDate: String): String {
        val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
        val accessor = timeFormatter.parse(inputDate)
        var date: Date = Date.from(Instant.from(accessor))
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(date)
        return currentDate
    }

    /**
     * UTC String 데이터를 받아
     * @return String yyyy 년도만 돌려
     */
    fun formatYearDate(inputDate: String): String {
        val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
        val accessor = timeFormatter.parse(inputDate)
        var date: Date = Date.from(Instant.from(accessor))
        val sdf = SimpleDateFormat("yyyy")
        val currentDate = sdf.format(date)
        return currentDate
    }

    /**
     * UTC String 데이터를 받아
     * @return String kk:mm 형태로 돌려줌
     */
    fun formatDateHHMM(inputDate: String): String {
        val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
        val accessor = timeFormatter.parse(inputDate)
        var date: Date = Date.from(Instant.from(accessor))
        val sdf = SimpleDateFormat("kk:mm")
        val currentDate = sdf.format(date)
        return currentDate
    }

    /**
     * UTC String 데이터를 받아
     * @return String yyyy.MM.dd 형태로 돌려줌
     */
    fun formatDateTimeDateDote(inputDate: String): String {
        val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
        val accessor = timeFormatter.parse(inputDate)
        var date: Date = Date.from(Instant.from(accessor))
        val sdf = SimpleDateFormat("yyyy.MM.dd")
        val currentDate = sdf.format(date)
        return currentDate
    }

    /**
     * UTC String 데이터를 받아
     * @return String kk:mm:ss 형태로 돌려줌
     */
    fun formatDateTimeDate(inputDate: String): String {
        val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
        val accessor = timeFormatter.parse(inputDate)
        var date: Date = Date.from(Instant.from(accessor))
        val sdf = SimpleDateFormat("kk:mm:ss")
        val currentDate = sdf.format(date)
        return currentDate
    }

    /**
     * 시간 초 데이터를 받아서
     * @return String HH:mm:ss 형태로 돌려줌
     */
    fun convertHMS(seconds: Int): String? {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val time: LocalTime = LocalTime.MIN.plusSeconds(seconds.toLong())
        return formatter.format(time)
    }

    /**
     * 시간 초 데이터를 받아서
     * @return String XX분  XX초  또는 XX초로 돌려줌
     */
    fun covertMinStringData(seconds: Int): String {
        var m = seconds / 60
        val s = seconds % 60
        if (m > 0) {
            return String.format(Locale.KOREA, "%02d분 %02d초", m, s)
        } else {
            return String.format(Locale.KOREA, "%02d초", seconds)
        }
    }



}