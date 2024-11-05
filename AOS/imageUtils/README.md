내가 작업했지만, 솔직히 다른 이미지 라이브러리 쓰자.


```

/****
 *  ImageUtils
 *  카메라 또는 앨범 이미지를 가져와서 회전 시키는 유틸 액티비티
 */
class ImageUtils : BaseActivity<ActivityImageUtilsBinding>({
    ActivityImageUtilsBinding.inflate(it)
}) {

    override fun preLoad() {

    }

    var currentRotation = 0f //화면 전환용 변수
    var imagesUri: Uri? = null //이미지 Uri

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            return
        }
    }

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagesUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("imageUri", Uri::class.java)
        } else {
            intent.getParcelableExtra("imageUri") as? Uri
        }

        grantUriPermission(
            this@ImageUtils.packageName,
            imagesUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )


        Glide.with(this).load(imagesUri).fitCenter().into(binding.initImageView)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }



    /**
     * rotationButtonEvent
     * 화면을 회전 시키는 버튼 이벤트
     */
    fun rotationButtonEvent(v: View) {

        val windowMetrics =
            WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this@ImageUtils)
        val currentBounds = windowMetrics.bounds
        val deviceWidth = currentBounds.width()
        val deviceHeight = currentBounds.height()

        val checkRotationData = binding.initImageView.rotation

        if (checkRotationData % 90 == 0f) {
            currentRotation = binding.initImageView.rotation
            val currentImageViewHeight = binding.initImageView.height

            val heightGap: Int = if (currentImageViewHeight > deviceHeight) {
                deviceWidth - currentImageViewHeight
            } else {
                deviceHeight - currentImageViewHeight
            }

            if (currentRotation % 90 == 0f) {
                ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 500
                    addUpdateListener {
                        val animationValue = it.animatedValue as Float
                        binding.initImageView.run {
                            layoutParams.height =
                                currentImageViewHeight + (heightGap * animationValue).toInt()
                            rotation = currentRotation + 90 * animationValue
                            requestLayout()
                        }
                    }
                }.start()
            }
        }
    }

    /**
     * saveButtonEvent
     * 화면에 표기된 이미지를 저장 시키는 이벤트
     */
    fun saveButtonEvent(v: View) {

        imagesUri?.let {
            if (it != Uri.EMPTY) { //Uri 값 공백 체크
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))

                try {
                    //카메라 Uri 처리
                    val exif = ExifInterface(
                        this.contentResolver?.openFileDescriptor(
                            it,
                            "rw",
                            null
                        )!!.fileDescriptor
                    )

                    val exifOrientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )

                    val angle = when (exifOrientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }

                    val initViewRotation = binding.initImageView.rotation
                    val currentRotationData = initViewRotation + angle

                    initImage(bitmap, currentRotationData)

                } catch (e: Exception) {
                    //앨범 Uri 처리
                    val imageUri = getRealPathFromUri(this@ImageUtils, it)
                    val angle = getOrientationOfImage(imageUri)
                    val initViewRotation = binding.initImageView.rotation
                    val currentRotationData = initViewRotation + angle
                    initImage(bitmap, currentRotationData)
                }

            }
        }

    }

    /**
     * getRotatedBitmap
     * 비트맵 파일과, 회전각을 받아 새로운 비트맵을 생성하는 메소드
     * @param bitmap
     * @param degrees
     */
    private fun getRotatedBitmap(bitmap: Bitmap?, degrees: Float): Bitmap? {
        if (bitmap == null) return null
        if (degrees == 0F) return bitmap
        val m = Matrix()
        m.setRotate(degrees, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    /**
     * initImage
     * 이미지 생성
     * @author finalBitmap가 생성 되고 saveImage가 호출됨
     */
    fun initImage(bitmap: Bitmap?, currentRotation: Float) {
        val finalBitmap = getRotatedBitmap(bitmap, currentRotation)
        finalBitmap?.let {
            saveImage(it)
        }
    }

    /**
     * saveImage
     * 카메라 또는 이미지 앨범 선택 이후 최종적으로 파일을 생성하는 곳
     * @param finalBitmap 최종 생성된 비트맵 파일
     */
    private fun saveImage(finalBitmap: Bitmap) {
        val myDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/앱이름 넣어도 되고 딴거 해도 되고")
        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera")
        myDir?.mkdirs()
        val fileName = imagesUri?.let { getFileName(it) }

        fileName?.let {
            val file = File(myDir, it)
            if (file.exists()) file.delete()
            try {
                val out = FileOutputStream(file)
                val finalUriData = Uri.fromFile(file)
                scanFile(this@ImageUtils, file, "image/*")
                finalBitmap.compress(CompressFormat.JPEG, 100, out)
                //var testUri = addImageToGallery(this@ImageUtils.contentResolver, "png", file, it);
                MediaStore.Images.Media.insertImage(contentResolver, finalBitmap, fileName, null)
                out.flush()
                out.close()
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("createImageUri", finalUriData)
                }
                setResult(RESULT_OK, intent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("errorData", e.toString())
                    putExtra("errorUri",imagesUri)
                }
                setResult(RESULT_CANCELED, intent)
                finish()
            }
        }

    }

    /**
     * getFileName
     * uri에서 파일명을 가지고 오는 메소드
     * @param uri 파일의 uri
     * @return String 파일명
     */
    @SuppressLint("Range")
    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }


    /**
     * getRealPathFromUri
     * uri에서 파일명을 가지고 오는 메소드
     * @param context Context
     * @param contentUri 파일 Uri
     * @return String 파일 패치
     */
    fun getRealPathFromUri(context: Context, contentUri: Uri?): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }


    /**
     * getRealPathFromUri
     * uri에서 파일명을 가지고 오는 메소드
     * @param filepath 파일 패치 값
     * @return Int 이미지의 각도를 되돌려줌
     */
    fun getOrientationOfImage(filepath: String?): Int {
        var exif: ExifInterface? = null

        try {
            exif = ExifInterface(filepath!!)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("intescase", "exif e :" + e.toString())
            return -1
        }

        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)

        if (orientation != -1) {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> return 90

                ExifInterface.ORIENTATION_ROTATE_180 -> return 180

                ExifInterface.ORIENTATION_ROTATE_270 -> return 270
            }
        }

        return 0
    }


    /**
     * scanFile
     * MediaScannerConnection 파일을 추가 삭제 시 파일을 스캔 하도록 하는 메소드
     * @param context 파일 패치 값
     * @param f File 찾을 파일
     * @param mimeType 스캔을 돌릴 타입
     */
    fun scanFile(context: Context, f: File, mimeType: String) {
        MediaScannerConnection.scanFile(context, arrayOf(f.absolutePath), arrayOf(mimeType), null)
    }


    fun addImageToGallery(
        cr: ContentResolver,
        imgType: String,
        filepath: File,
        fileName: String
    ): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, fileName)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.DESCRIPTION, "")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATA, filepath.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES
            )
            values.put(MediaStore.MediaColumns.IS_PENDING, 1)
            val uri =
                cr.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), values)
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            cr.update(uri!!, values, null, null)
            return uri
        } else {
            return cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }
}


```

화면 XML

```
<?xml version="1.0" encoding="utf-8"?>


<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/black"
        tools:context=".utils.image.ImageUtils">


        <ImageView
            android:id="@+id/init_image_view"
            android:layout_width="0dp"
            android:layout_height="400dp"
            android:scaleType="center"
            app:layout_constraintBottom_toTopOf="@+id/image_rotation_button"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <TextView
            android:id="@+id/image_rotation_button"
            android:layout_width="0dp"
            android:layout_height="80dp"
            android:background="@color/black"
            android:clickable="true"
            android:enabled="true"
            android:focusable="true"
            android:gravity="center"
            android:onClick="rotationButtonEvent"
            android:paddingStart="9dp"
            android:paddingTop="5dp"
            android:paddingEnd="9dp"
            android:paddingBottom="5dp"
            android:text="이미지 회전"
            android:textColor="@color/white"
            android:textSize="18sp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toStartOf="@+id/image_save_button"
            app:layout_constraintStart_toStartOf="parent" />


        <TextView
            android:id="@+id/image_save_button"
            android:layout_width="0dp"
            android:layout_height="80dp"
            android:background="@color/black"
            android:clickable="true"
            android:enabled="true"
            android:focusable="true"
            android:gravity="center"
            android:onClick="saveButtonEvent"
            android:paddingStart="9dp"
            android:paddingTop="5dp"
            android:paddingEnd="9dp"
            android:paddingBottom="5dp"
            android:text="이미지 저장"
            android:textColor="@color/white"
            android:textSize="18sp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toEndOf="@+id/image_rotation_button" />


    </androidx.constraintlayout.widget.ConstraintLayout>

</layout>
```
