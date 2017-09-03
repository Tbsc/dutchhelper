package tbsc.dutchhelper.util.wikt.handlers;

import de.tudarmstadt.ukp.jwktl.api.util.TemplateParser;

/**
 * Handler for {{ux}} (example template).
 * Test with geloven.
 *
 * This isn't an example handler, this is a handler for the example template.
 *
 * Created on 19/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class ExampleTemplateHandler extends BaseTemplateHandler {

    @Override
    protected String performAction(TemplateParser.Template template) {
        String result = template.getNumberedParam(1);
        String translation = "";

        if (template.getNamedParam("translation") != null) {
            translation = "\n        " + template.getNamedParam("translation");
        }

        if (template.getNamedParam("t") != null) {
            translation = "\n        " + template.getNamedParam("t");
        }

        // trying to see if param exists by null checking causes NPE, so checking by count it is
        if (template.getNumberedParamsCount() > 2) {
            translation = "\n        " + template.getNumberedParam(2);
        }

        return result + translation;
    }

    @Override
    protected String[] getTemplates() {
        return new String[] {
                "ux", "eg", "usex"
        };
    }

}
