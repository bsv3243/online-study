package seong.onlinestudy.docs;

import org.springframework.restdocs.snippet.Attributes.Attribute;

import static org.springframework.restdocs.snippet.Attributes.key;

public interface DocumentFormatGenerator {

    static Attribute getDateFormat() {
        return key("format").value("yyyy-MM-dd");
    }

    static Attribute getDefaultValue(String value) {
        return key("defaultValue").value(value);
    }

    static Attribute getConstraint(String value) {
        return key("constraint").value(value);
    }
}
