package seong.onlinestudy.enumtype;

public enum GroupRole implements EnumType {
    MASTER("그룹장"),
    USER("그룹원");

    private final String text;

    GroupRole(String text) {
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
