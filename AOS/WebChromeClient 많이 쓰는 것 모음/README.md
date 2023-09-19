# webview 대부분 많이 쓰는 것들

ifram형 유튜브 전체화면 및 
input type="file" 태그 
권환 호출 후 사용 하는 것들 모음


binding.webView.webChromeClient = object : WebChromeClient() {
            //
            //웹뷰에 alert창에 url을 제거한다.
            //
            override fun onJsAlert(
                view: WebView, url: String, message: String,
                result: JsResult
            ): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("")
                    .setMessage(message)
                    .setPositiveButton(
                        android.R.string.ok
                    ) { dialog, which -> result.confirm() }
                    .setCancelable(false)
                    .create()
                    .show()
                return true
            }

            //
            //웹뷰에 Confirm창에 url을 제거한다.
            //
            override fun onJsConfirm(
                view: WebView, url: String,
                message: String, result: JsResult
            ): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("")
                    .setMessage(message)
                    .setPositiveButton(
                        android.R.string.ok
                    ) { dialog, which -> result.confirm() }
                    .setNegativeButton(
                        android.R.string.cancel
                    ) { dialog, which -> result.cancel() }
                    .setCancelable(false)
                    .create()
                    .show()
                return true
            }

            // input type="file" 태그 인식 하는 곳
            override fun onShowFileChooser(
                webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {

                //권환 호출 후 파일 선택기로 돌림
                return true
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    if (mCustomView != null) {
                        callback!!.onCustomViewHidden()
                        return
                    }

                    this@MainActivity.requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE

                    mOriginalOrientation = this@MainActivity.requestedOrientation
                    val decor = this@MainActivity.window?.decorView as FrameLayout
                    mFullscreenContainer = FullscreenHolder(this@MainActivity)
                    mFullscreenContainer!!.addView(view, COVER_SCREEN_PARAMS)
                    decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS)
                    mCustomView = view
                    setFullscreen(true)
                    mCustomViewCallback = callback }

                super.onShowCustomView(view, callback)
            }

            override fun onShowCustomView(
                view: View?,
                requestedOrientation: Int,
                callback: WebChromeClient.CustomViewCallback
            ) {
                super.onShowCustomView(view, requestedOrientation, callback)
            }

            override fun onHideCustomView() {
                if (mCustomView == null) {
                    return
                }

                setFullscreen(false)
                val decor = this@MainActivity.window.decorView as FrameLayout
                decor.removeView(mFullscreenContainer)
                mFullscreenContainer = null
                mCustomView = null
                mCustomViewCallback!!.onCustomViewHidden()
                this@MainActivity.requestedOrientation = SCREEN_ORIENTATION_PORTRAIT

            }

        }
