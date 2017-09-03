package tbsc.dutchhelper.util.wikt;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Wiktionary templates, such as {{lb}}, [[]], and more.
 * This class is supposed to work as a one-for-all-cases-class (great name):
 * it works for all cases, and all you need to do is to tell it what to do.
 *
 * DO NOT USE THIS CLASS! Instead, create a handler or use a pre-existing one.
 *
 * Created on 17/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class TemplateParserOld {

    /**
     * Main parser method.
     *
     * @param template The template to parse
     * @param wikiText Direct output from Wiktionary to parse
     * @return Parsed wikitext, with the template as the first entry (key being the template and value the regex match.
     *         Parameters without keys are assigned their index. Doesn't split values.
     *         If a match wasn't found, returns an empty list
     */
    public static LinkedHashMap<String, String> parse(String template, String wikiText) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();

        // create regex pattern for getting pre-parsed parameters
        Pattern pattern = Pattern.compile("(\\{\\{" + Pattern.quote(template) + "\\|(.+?)}})");
        Matcher match = pattern.matcher(wikiText);

        if (match.find()) {
            // add the template and the matching wikitext
            result.put(template, match.group(1));

            String parameters = match.group(2);
            // each parameter is separated with a pipe character
            String[] parameterArray = StringUtils.split(parameters, "|");

            for (int i = 0; i < parameterArray.length; ++i) {
                String parameter = parameterArray[i];
                if (parameter.contains("=")) {
                    // parameter is a key-value parameter, get key and value separately
                    String[] parameterKeyValue = StringUtils.split(parameter, "=");
                    result.put(parameterKeyValue[0], parameterKeyValue[1]);
                } else {
                    // parameter doesn't have a key, put in map with it's count
                    // adding a one is necessary to follow Wiktionary guidelines
                    result.put(String.valueOf(i + 1), parameter);
                }
            }
        }

        // always returns the initialized map, even if empty
        return result;
    }

    /**
     * Splits the value of each parameter from {@link #parse(String, String)}.
     * @param template The template to parse
     * @param wikiText Direct output from Wiktionary to parse
     * @return Parsed wikitext, not including the template itself. Parameters without keys are assigned their index. Splits values if possible.
     */
    public static LinkedHashMap<String, List<String>> parseMultiValue(String template, String wikiText) {
        LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
        // skips the template entry because it will definitely have a pipe char and we shouldn't split it
        parse(template, wikiText).forEach((key, value) ->
                result.put(key,
                        !key.equals(template)
                                ? Arrays.asList(StringUtils.split(value, "|"))
                                : Collections.singletonList(value)));
        return result;
    }

}
