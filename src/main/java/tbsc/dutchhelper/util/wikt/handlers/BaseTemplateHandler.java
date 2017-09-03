package tbsc.dutchhelper.util.wikt.handlers;

import de.tudarmstadt.ukp.jwktl.api.util.TemplateParser;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Base class for template handlers, that abstracts out checking for the template.
 *
 * Created on 19/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public abstract class BaseTemplateHandler implements TemplateParser.ITemplateHandler {

    @Override
    public String handle(TemplateParser.Template template) {
        if (ArrayUtils.contains(getTemplates(), template.getName())) {
            return performAction(template);
        }

        return null;
    }

    /**
     *
     * @param template Template object containing parameters
     * @return String to replace the template with, or null if replacing shouldn't be done
     */
    protected abstract String performAction(TemplateParser.Template template);

    /**
     * Used by the base class to know if it should call {@link #performAction(TemplateParser.Template)}.
     * @return String array containing all templates that should activate this template handler
     */
    protected abstract String[] getTemplates();

}
