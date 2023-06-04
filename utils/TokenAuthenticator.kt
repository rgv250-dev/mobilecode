//개인적으로는 정상적인 방식으로 토큰 교체방식을 썻으면 함 

class TokenAuthenticator(
    var mContext: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401 || response.code == 500) {
            val token = SharedUtils(mContext).getString(Utils.TOKEN, "")
            val refreshToken = SharedUtils(mContext).getString(Utils.REFRESH_TOKEN, "")
            var data = 호출 하는 API .execute() //실행
            if (data.isSuccessful) {

                if (accessToken.isNullOrEmpty()) {
                        //토큰 못받아 오는 경우에 처리 200일 떨어졌는데 없는 경우도 있을 수 있으니
                } else {
                    //받으면 처리 할 거
                }

                if (data.body()?.data?.refreshToken.isNullOrEmpty()) {
                   //리플래시 토큰 못받았을때 
                } else {
                    //받으면 처리 할 거
                }

                if (data.body()?.data?.accessToken.isNullOrEmpty()) {
                    response.close() // 
                } else {
                    response.close()
                    val refreshedRequest = chain.request()
                        .putHeader(액세스토큰)
                    return chain.proceed(refreshedRequest)
                }

            } else {
                //데이터 반응 아예 없을때
                response.close()
            }
        }

        return response
    }

    private fun Request.putHeader(mobileToken: String): Request {
        return this.newBuilder()
            .removeHeader("Authorization") //이미 들어간 헤더값 제거
            .addHeader("Authorization", mobileToken)
            .build()
    }


}