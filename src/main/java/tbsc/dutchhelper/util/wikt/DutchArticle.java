package tbsc.dutchhelper.util.wikt;

import de.tudarmstadt.ukp.jwktl.api.util.GrammaticalGender;

/**
 * Util class for working with Dutch genders and articles.
 *
 * Created on 08/08/2017
 * @author tbsc
 */
public enum DutchArticle {

    DE("de"), HET("het"), NONE("n/a");

    private String readable;

    DutchArticle(String readable) {
        this.readable = readable;
    }

    public String getText() {
        return readable;
    }

    public static DutchArticle fromGender(GrammaticalGender gender) {
        if (gender == null) {
            return NONE;
        }

        switch (gender) {
            case NEUTER:
                return HET;
            case FEMININE:
            case MASCULINE:
                return DE;
        }
        return NONE;
    }

}
