package tbsc.dutchhelper.util.wikt.handlers;

import de.tudarmstadt.ukp.jwktl.api.util.TemplateParser;

/**
 * Template handler for printing all numbered parameters of a template, starting from a specific index, and printing
 * a prefix and/or a suffix.
 *
 * Created on 18/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class ListBaseTemplateHandler extends BaseTemplateHandler {

    public ListBaseTemplateHandler(String... templates) {
        this("", templates);
    }

    public ListBaseTemplateHandler(int start, String... templates) {
        this(start, "", templates);
    }

    public ListBaseTemplateHandler(String prefix, String[] templates) {
        this(prefix, 0, templates);
    }

    public ListBaseTemplateHandler(int start, String suffix, String[] templates) {
        this("", suffix, start, templates);
    }

    public ListBaseTemplateHandler(String prefix, int start, String... templates) {
        this(prefix, "", start, templates);
    }

    public ListBaseTemplateHandler(String prefix, String suffix, int start, String... templates) {
        this.startIndex = start;
        this.prefix = prefix;
        this.suffix = suffix;
        this.templates = templates;
    }

    public int startIndex = 0;
    public String prefix = "";
    public String suffix = "";
    public String[] templates = {};

    @Override
    protected String performAction(TemplateParser.Template template) {
        StringBuilder result = new StringBuilder();

        // result.append(prefix);

        // starting where we're told to
        for (int i = startIndex; i < template.getNumberedParamsCount(); ++i) {
            result.append(template.getNumberedParam(i));
            if (i < template.getNumberedParamsCount() - 1) {
                result.append(", ");
            }
        }

        // result.append(suffix);

        String strResult = result.toString();
        return "".equals(strResult) ? null : prefix + strResult + suffix;
    }

    @Override
    protected String[] getTemplates() {
        return templates;
    }
}
