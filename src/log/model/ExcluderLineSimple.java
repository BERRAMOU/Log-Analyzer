package log.model;

import log.Settings;
import log.LEUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aziz on 30/12/2015.
 */
public class ExcluderLineSimple extends AbstractExcluder  {

    private List<String> excludeSimple;

    private static class ExcluderLineHolder {
        private static final ExcluderLineSimple INSTANCE = new ExcluderLineSimple();
    }

    public static ExcluderLineSimple getInstance() {
        return ExcluderLineHolder.INSTANCE;
    }

    private ExcluderLineSimple(){
        super();
        this.excludeSimple=new ArrayList<String>();
        try{
            loadPattern(Settings.getInstance().getExcludeLineSimpleFile());
        } catch (IOException e) {
            e.printStackTrace();
            LEUtil.logError("ExcluderLineSimple:ExcluderLineSimple(): " + e);
        }
    }

    public List<String> getExcludePattern(){
        return excludeSimple;
    }
    @Override
    protected void addPattern(String exclude) {
        excludeSimple.add(exclude);
    }
}
