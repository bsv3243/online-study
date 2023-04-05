package seong.onlinestudy.enumtype;

public enum PostCategory implements EnumType {
    INFO("정보"),
    QUESTION("질문"),
    CHAT("잡담");

    private final String text;

    PostCategory(String text) {
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
