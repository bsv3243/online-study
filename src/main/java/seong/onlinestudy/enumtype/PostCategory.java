package seong.onlinestudy.enumtype;

public enum PostCategory {
    INFO("정보"),
    QUESTION("질문"),
    CHAT("잡담");

    private final String description;

    PostCategory(String description) {
        this.description = description;
    }
}
