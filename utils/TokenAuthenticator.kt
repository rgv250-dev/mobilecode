class TokenAuthenticator(
    var mContext: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            val token = SharedUtils(mContext).getString(Utils.TOKEN, "")
            val refreshToken = SharedUtils(mContext).getString(Utils.REFRESH_TOKEN, "")
            var data = 호출 하는 API .execute() //실행
            if (data.isSuccessful) {

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
            .addHeader("Authorization", "Bearer " + mobileToken)
            .build()
    }


}
