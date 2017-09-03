package tbsc.dutchhelper.util.wikt.handlers;

import de.tudarmstadt.ukp.jwktl.api.util.TemplateParser;

/**
 * Base class for templates that should be replaced with a numbered parameter's value.
 * Lets you add a prefix and/or a suffix, and choose which numbered parameter will replace the template.
 *
 * Created on 19/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class NumParamBaseTemplateHandler extends BaseTemplateHandler {

    public NumParamBaseTemplateHandler(String... templates) {
        this("", templates);
    }

    public NumParamBaseTemplateHandler(int paramIndex, String... templates) {
        this(paramIndex, "", templates);
    }

    public NumParamBaseTemplateHandler(String prefix, String[] templates) {
        this(prefix, 0, templates);
    }

    public NumParamBaseTemplateHandler(int paramIndex, String suffix, String[] templates) {
        this("", suffix, paramIndex, templates);
    }

    public NumParamBaseTemplateHandler(String prefix, int paramIndex, String... templates) {
        this(prefix, "", paramIndex, templates);
    }

    public NumParamBaseTemplateHandler(String prefix, String suffix, int paramIndex, String... templates) {
        this.paramIndex = paramIndex;
        this.prefix = prefix;
        this.suffix = suffix;
        this.templates = templates;
    }

    public int paramIndex = 0;
    public String prefix = "";
    public String suffix = "";
    public String[] templates = {};

    @Override
    protected String performAction(TemplateParser.Template template) {
        String param = template.getNumberedParam(paramIndex);
        if (param == null) {
            return null;
        }
        return prefix + template.getNumberedParam(paramIndex) + suffix;
    }

    @Override
    protected String[] getTemplates() {
        return templates;
    }

}
