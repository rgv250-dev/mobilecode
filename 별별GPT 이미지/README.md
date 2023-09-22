# 별별GPT 이미지 및 간략한 서비스 내용

가상화에 안드로이드 OS 13 + 메신저R + 카카오봇 제작
스크린샷 

장점 : 빠르게 연동 가능하고 사용자는 어떤 시스템을 쓰는지 알 수 없기 때문에 서비스를 이용하는데 큰 문제가 되지 않는다

현재는 채팅 내역을 읽고 답변 기능만 추가되어 나머지 옵션 값을 호출 하여도 답변하지 않습니다.

메신저 R 소스 요약 
const scriptName = “”; //스크립트 제목
/**
* (string) room
* (string) sender
* (boolean) isGroupChat
* (void) replier.reply(message)
* (boolean) replier.reply(room, message, hideErrorToast = false) // 전송 성공시 true, 실패시 false 반환
* (string) imageDB.getProfileBase64()
* (string) packageName
*/
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {

replier.reply(“” + getResponse(msg)); //메신저 R이 메세지를 검출 후 호출하는 곳

}


function getResponse(msg) {

let json;
let result;

try {

let data = {“message”:msg,
“temperature”: 0.1 // 사용할 옵션 값정의 
};

let response = org.jsoup.Jsoup.connect(“”) //링크 제거 [보통 오픈 API 주소나 관련 주소 추가 오픈 API에 경우 토큰 필요] 
.header(“Content-Type”, “application/json”) //헤더 
.requestBody(JSON.stringify(data))
.ignoreContentType(true)
.ignoreHttpErrors(true)
.timeout(200000)
.post();
json = JSON.parse(response.text());
result = json.message;


} catch(e){


result = e;
Log.e(e);

}
return result;


}

function onCreate(savedInstanceState, activity) {}

function onStart(activity) {}

function onResume(activity) {}

function onPause(activity) {}

function onStop(activity) {}



# 스크린샷
![이미지] ./2.png
![이미지2] ./3.png

