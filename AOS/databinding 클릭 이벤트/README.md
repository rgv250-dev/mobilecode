데이터 바인딩 클릭 이벤트


```

xml 

<layout>

<data>
        <variable
            name="handlers" 
            type="com.test.login.LoginActivity" />
</data>

<!--디자인 생략-->

<!--적용할 곳-->
<TextView
            android:id="@+id/login_ok_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:layout_marginTop="16dp"
            android:layout_marginEnd="16dp"
            android:fontFamily="@font/pretendard_bold"
            android:gravity="center"
            android:paddingTop="16dp"
            android:paddingBottom="16dp"
            android:text="@string/btn_ok"
            android:textColor="@color/white"
            android:textSize="18sp"
            android:clickable="true"
            android:onClick="@{()->handlers.onOnClickOkButton()}"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/login_password_edittext_layout" />

<!--디자인 생략-->

</layout>



class LoginActivity 전체 소스 생략

```
 override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.handlers = this


```



```

fun onOnClickOkButton() {
        //작업 진행 되어야 하는 곳
}

//뷰를 써야 하는 경우에는 뷰에서 데이터를 받거나 또는 필요에 따라 써야 하는 경우에는
//xml에 
android:onClick="@{handlers::onOnClickOkButton}"

//코드에
fun onOnClickOkButton(view: View){
//작업 진행 되어야 하는 곳
}

```

이렇게 사용하면 된다.



