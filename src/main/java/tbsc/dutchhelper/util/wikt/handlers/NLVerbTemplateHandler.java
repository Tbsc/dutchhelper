package tbsc.dutchhelper.util.wikt.handlers;

import de.tudarmstadt.ukp.jwktl.api.util.TemplateParser;
import org.apache.commons.lang3.StringUtils;

/**
 * Handles {{nl-verb form of}} (verb inflection template)
 * Test with any non-infinitive verb form.
 *
 * Created on 18/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class NLVerbTemplateHandler extends BaseTemplateHandler {

    @Override
    protected String performAction(TemplateParser.Template template) {
        StringBuilder result = new StringBuilder();

        if (template.getNamedParam("p") != null) {
            char[] persons = template.getNamedParam("p").toCharArray();
            for (int i = 0; i < persons.length; i++) {
                char personChar = persons[i];
                if (personChar == '1') {
                    result.append("first-");
                } else if (personChar == '2') {
                    result.append("second-");
                } else if (personChar == '3') {
                    result.append("third-");
                }
                // only add "and" if it's not the last person
                if (i < persons.length - 1) {
                    result.append(" and ");
                }
            }
            result.append("person ");
        }

        if (template.getNamedParam("n") != null) {
            if (template.getNamedParam("n").equals("sg")) {
                result.append("singular ");
            } else if (template.getNamedParam("n").equals("pl")) {
                result.append("plural ");
            }
        }

        if (template.getNamedParam("t") != null) {
            if (template.getNamedParam("t").equals("pres")) {
                result.append("present ");
            } else if (template.getNamedParam("t").equals("past")) {
                result.append("past ");
            }
        }

        if (template.getNamedParam("m") != null) {
            String[] moods = StringUtils.split(template.getNamedParam("m"), "+");
            for (int i = 0; i < moods.length; i++) {
                String mood = moods[i];
                switch (mood) {
                    case "imp":
                        // if it's imperative, it's archaic and that should be noted at the start
                        String tmp = result.toString();
                        result = new StringBuilder("(archaic) " + tmp);
                        result.append("imperative ");
                        break;
                    case "subj":
                        result.append("subjunctive ");
                        break;
                    case "ptc":
                        result.append("participle ");
                        break;
                    case "ind":
                        result.append("indicative ");
                        break;
                }

                // only add "and" if it's not the last mood
                if (i < moods.length - 1) {
                    result.append("and ");
                }
            }
        }

        // template specification says that the infinitive must be the first numbered parameter
        result.append("of ").append(template.getNumberedParam(0));
        // new ArrayList<>(optSplitMap.keySet()).get(optSplitMap.size() - 1));

        return result.toString();
    }

    @Override
    protected String[] getTemplates() {
        return new String[] {
                "nl-verb form of"
        };
    }

}
