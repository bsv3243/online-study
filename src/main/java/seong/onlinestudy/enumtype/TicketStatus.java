package seong.onlinestudy.enumtype;

public enum TicketStatus {
    STUDY(Values.STUDY),
    REST(Values.REST);

    private String value;

    TicketStatus(String value) {
        if(!this.name().equals(value)) {
            throw new IllegalArgumentException("name 값과 일치하지 않습니다.");
        }
    }

    public static class Values {
        public static final String STUDY = "STUDY";
        public static final String REST = "REST";
    }
}
