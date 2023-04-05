package seong.onlinestudy.converter;

import org.springframework.core.convert.converter.Converter;
import seong.onlinestudy.enumtype.TicketStatus;

public class StringToTicketStatusConverter implements Converter<String, TicketStatus> {

    @Override
    public TicketStatus convert(String source) {
        return TicketStatus.valueOf(source);
    }
}
