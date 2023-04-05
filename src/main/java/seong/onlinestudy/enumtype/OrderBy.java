package seong.onlinestudy.enumtype;

public enum OrderBy implements EnumType{
    CREATEDAT("생성일"),
    MEMBERS("회원"),
    TIME("공부시간"),
    ATTENDANCE("출석률");

    private final String text;

    OrderBy(String text) {
        this.text = text;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getText() {
        return this.text;
    }
}
