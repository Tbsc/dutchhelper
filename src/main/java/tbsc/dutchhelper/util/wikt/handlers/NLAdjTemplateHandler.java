package tbsc.dutchhelper.util.wikt.handlers;

import de.tudarmstadt.ukp.jwktl.api.util.TemplateParser;

/**
 * Handler for {{nl-adj form of}} (adjective inflection template)
 * Test with any adjective.
 *
 * Created on 19/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class NLAdjTemplateHandler extends BaseTemplateHandler {

    @Override
    protected String performAction(TemplateParser.Template template) {
        String word = template.getNumberedParam(1);

        String result = "";

        switch (template.getNumberedParam(0)) {
            case "infl":
                result = "inflected form of " + word;
                break;
            case "part":
                result = "partitive form of " + word;
                break;
            case "pred":
                result = "predicative form of " + word;
                break;
            case "comp":
                result = "comparative form of " + word;
                break;
            case "sup":
                result = "superlative form of " + word;
                break;
        }

        if (template.getNamedParam("comp-of") != null) {
            result += ", the comparative of " + template.getNamedParam("comp-of");
        }

        if (template.getNamedParam("sup-of") != null) {
            result += ", the superlative of " + template.getNamedParam("sup-of");
        }

        return result;
    }

    @Override
    protected String[] getTemplates() {
        return new String[] {
                "nl-adj form of"
        };
    }

}
