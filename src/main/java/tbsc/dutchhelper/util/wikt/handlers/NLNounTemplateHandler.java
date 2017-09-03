package tbsc.dutchhelper.util.wikt.handlers;

import de.tudarmstadt.ukp.jwktl.api.util.TemplateParser;

/**
 * Handler for {{nl-noun form of}} (noun form template)
 * Test with any plural/diminutive noun.
 *
 * Created on 18/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class NLNounTemplateHandler extends BaseTemplateHandler {

    @Override
    protected String performAction(TemplateParser.Template template) {
        String word = template.getNumberedParam(1);
        switch (template.getNumberedParam(0)) {
            case "dim":
                return "diminutive of " + word;
            case "pl":
                return "plural form of " + word;
            case "acc":
                return "(archaic) accusative form of " + word;
            case "gen":
                return "(archaic) genitive form of " + word;
            case "dat":
                return "(archaic) dative form of " + word;
        }

        return null;
    }

    @Override
    protected String[] getTemplates() {
        return new String[] {
                "nl-noun form of"
        };
    }

}
