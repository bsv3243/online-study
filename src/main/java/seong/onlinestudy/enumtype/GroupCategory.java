package seong.onlinestudy.enumtype;

public enum GroupCategory implements EnumType {
    JOB("취업"),
    GOV("공무원"),
    UNIV("대학생"),
    HIGH("고등학생"),
    BOOK("독서"),
    LANG("어학"),
    CERT("자격증"),
    IT("IT"),
    ETC("기타");

    private final String text;

    GroupCategory(String text) {
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
