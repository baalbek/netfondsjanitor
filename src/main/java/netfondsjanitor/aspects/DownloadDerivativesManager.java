package netfondsjanitor.aspects;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import netfondsjanitor.exceptions.EtradeJanitorException;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 5/10/13
 * Time: 10:20 AM
 */

@Aspect
public class DownloadDerivativesManager {

    private Logger log = Logger.getLogger(getClass().getPackage().getName());

    private String feedStoreDir;
    private String dateFeedStoreDir = null;

    private Function<Object,String> tickerFileNamer;

    @Pointcut("execution(@oahu.annotations.StoreHtmlPage * *(..))")
    public void storeHtmlPagePointcut() {
    }

    @Around("storeHtmlPagePointcut()")
    public Object store2htmlPointcutMethod(ProceedingJoinPoint jp) throws Throwable {
        HtmlPage result = (HtmlPage)jp.proceed();


        Object[] args = jp.getArgs();

        /*
        LocalTime t = LocalTime.now();
        int h = t.getHour();
        int m = t.getMinute();

        String hs = h < 10 ? String.format("0%d",h) : String.format("%d",h);
        String ms = m < 10 ? String.format("0%d",m) : String.format("%d",m);
        String fileName = String.format("%s/%s-%s_%s.html",getDateFeedStoreDir(),args[0],hs,ms);

        log.info(String.format("Saving file to %s",fileName));
        */

        String fileName = String.format("%s/%s",getDateFeedStoreDir(),tickerFileNamer.apply(args[0]));

        File out = new File(fileName);

        try (FileOutputStream fop = new FileOutputStream(out)) {
            // if file doesn't exists, then create it
            if (!out.exists()) {
                out.createNewFile();
            }
            // get the content in bytes
            byte[] contentInBytes = result.getWebResponse().getContentAsString().getBytes();
            if (contentInBytes == null) {
                log.warn(String.format("Could not get bytes from %s",result.getUrl()));
            }
            else {
                fop.write(contentInBytes);
                fop.flush();
                fop.close();
            }
        }
        catch (IOException e) {
            log.warn(String.format("Could not save: %s", fileName));
        }

        return result;
    }

    //region Private Methods
    //endregion Private Methods

    //region Properties
    public String getFeedStoreDir() {
        return feedStoreDir;
    }

    public void setFeedStoreDir(String feedStoreDir) {
        this.feedStoreDir = feedStoreDir;
    }

    public String getDateFeedStoreDir() {
        if (dateFeedStoreDir != null) return dateFeedStoreDir;

        if (getFeedStoreDir() == null) {
            throw new EtradeJanitorException("Property feedStoreDir not set!");
        };
        LocalDate m = LocalDate.now();
        int year = m.getYear();
        int month = m.getMonthValue();
        int day = m.getDayOfMonth();
        dateFeedStoreDir = String.format("%s/%d/%d/%d",getFeedStoreDir(),year,month,day);
        File f = new File(dateFeedStoreDir);
        if (f.exists() == false) {
            f.mkdirs();
        }
        return dateFeedStoreDir;
    }

    //endregion Properties



}
