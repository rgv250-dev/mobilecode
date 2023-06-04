/**
 * 
 * 안드로이드에 커스텀 폰트를 적용 후 비밀번호 스타일 사용시 
*  관련폰트가 없어 공백이 표시되는 경우가 있을때 이것을 사용하면 *로 표기 처리해서 보여주는 법
 */

public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

    private class PasswordCharSequence implements CharSequence {
        private CharSequence mSource;
        public PasswordCharSequence(CharSequence source) {
            mSource = source; 
        }
        public char charAt(int index) {
            return '*'; //모든 입력된 텍스트를 *표기로 변경
        }
        public int length() {
            return mSource.length(); // Return default
        }
        public CharSequence subSequence(int start, int end) {
            return mSource.subSequence(start, end); 
        }
    }
}