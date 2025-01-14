솔직히 이게 맞나? 자신이 점점 없어진다. 뭐 그건 별개고 
사실 앱 푸쉬 서버가 따로 있어서 만들 수 있으면 참 좋겠지만, 아쉬운대로 쓸때 필요한 것이다.
장점은 DB에 저장 후에 보내서 나중에 로그에서 어떤걸 보냈는지 알 수 있다 정도? 

미리 당연히 알겠지만 FCM 리소스파일을 받아 둬야한다. 

그리고 대부분은 알겠지만 대부분 유저는 토큰은 만료 상태다. 슬프지만 진짜 인기 서비스가 아닌 이상 이런 경우가 많다. 
나중에 실패 한 것만 모아둔 DB조회 하면서 눈물을 흘리도록 하자  


```
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *  FCMConfig
 * FCM 사용을 위한 FCMConfig
 * **/

@Configuration
public class FCMConfig {

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {

        ClassPathResource resource = new ClassPathResource("firebase/어쩌고 저쩌고 하는 파일 받아서 추가 해야함.json");

        InputStream refreshToken = resource.getInputStream();

        FirebaseApp firebaseApp = null;

        List<FirebaseApp> firebaseAppList = FirebaseApp.getApps();

        if(firebaseAppList != null && !firebaseAppList.isEmpty()){
            for(FirebaseApp app : firebaseAppList){
                if(app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)){
                    firebaseApp = app;
                }
            }
        } else {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(refreshToken))
                    .build();
            firebaseApp = FirebaseApp.initializeApp(options);
        }

        return FirebaseMessaging.getInstance(firebaseApp);
    }

}



```



```

/**
 * FCMService
 *
 * FCM 서비스 클래스
 **/

@Component
@RequiredArgsConstructor
@Slf4j
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;

    Aps aps = Aps.builder().setBadge(1).build();
    ApnsConfig apnsConfig = ApnsConfig.builder().setAps(aps).build();
    /**
     * sendFCM FCM 전송 메소드
     * FCM 전송 하는 메소드
     *
     * @param title FCM 상단 타이틀명
     * @param body  FCM 하단 내용
     * @param token FCMToken 값
     * @param data
     * @param userId 유저 아이디
     * @return FCMResponseDto FCM 전송 후 결과에 대한  resCode, resMsg 돌려줌
     */
    public FCMResponseDto sendFCM(String title, String body, String token, Map<String, String> data, String userId) {

        // 알림 설정
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(data)
                .setApnsConfig(apnsConfig)
                .build();

        try {
            firebaseMessaging.send(message);
            return new FCMResponseDto("0000", "success");
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();
            return new FCMResponseDto("9999", errorCode.toString());
        }
    }
}



```


```
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

/// FCMResponseDto
/// FCM 전송 후 결과에 대한  resCode, resMsg 를 가지는 DTO
public class FCMResponseDto {
    //resCode 성공 0000, 실패시 9999
    private String resCode;
    //resMsg 에러 메세지 내역
    private String resMsg;
    /**
     * FCMResponseDto
     * FCM 전송 후 결과에 대한  resCode, resMsg 생성함
     * */
    public FCMResponseDto(String resCode, String resMsg) {
        this.resCode = resCode;
        this.resMsg = resMsg;
    }

}
```

대충 step이나 정크 쯤에서 보내고 난 후에 에러 내역을 받을때 

```
 FCMResponseDto response = fcmService.sendFCM(dto.getTitle(), dto.getBody(), dto.getToken(), dto.getData(), dto.getUser_id());
                            if (!response.getResCode().equals("0000")) {
                                //에러 일때 뭘 할거냐 에러 내역용 DB에 쌓거나 혹은 뭐 다른걸 하겠지 아마도
                            }

```