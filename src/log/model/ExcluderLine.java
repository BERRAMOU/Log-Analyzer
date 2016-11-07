package log.model;

import log.Settings;
import log.LEUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Aziz on 30/12/2015.
 */
public class ExcluderLine extends AbstractExcluder {
    private List<Pattern> excludePattern;

    private static class ExcluderLineHolder {
        private static final ExcluderLine INSTANCE = new ExcluderLine();
    }

    public static ExcluderLine getInstance() {
        return ExcluderLineHolder.INSTANCE;
    }

    private ExcluderLine(){
        super();

        this.excludePattern=new ArrayList<Pattern>();

        try {
            loadPattern(Settings.getInstance().getExcludeLineFile());
        } catch (IOException e) {
            e.printStackTrace();
            LEUtil.logError("ExcluderLine:ExcluderLine(): " + e);
        }

    }

    public List<Pattern> getExcludePattern() {
        return excludePattern;
    }

    private void addPattern(Pattern pattern){
        excludePattern.add(pattern);
    }

    @Override
    protected void addPattern(String regix) {
        addPattern(Pattern.compile(regix));
    }

}
