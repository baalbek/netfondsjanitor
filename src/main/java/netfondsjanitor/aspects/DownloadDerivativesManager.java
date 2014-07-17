package netfondsjanitor.aspects;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import oahu.financial.html.DownloadManager;
import netfondsjanitor.exceptions.EtradeJanitorException;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 5/10/13
 * Time: 10:20 AM
 */

@Aspect
public class DownloadDerivativesManager implements DownloadManager {

    private Logger log = Logger.getLogger(getClass().getPackage().getName());

    private WebClient webClient;

    private String feedStoreDir;
    private String dateFeedStoreDir = null;

    private Function<Object,String> tickerFileNamer;

    private Map<Object,HtmlPage> lastDownloads = new HashMap<Object,HtmlPage>();

    //region Pointcuts
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

        log.info(String.format("Saving downloaded %s to %s",args[0], fileName));

        lastDownloads.put(args[0], result);

        File out = new File(fileName);

        try (FileOutputStream fop = new FileOutputStream(out)) {
            // if file doesn't exists, then create it
            if (!out.exists()) {
                out.createNewFile();
            }
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
    //endregion Pointcuts

    //region Interface DownloadManager
    @Override
    public HtmlPage getLastDownloaded(String ticker) throws IOException {
        HtmlPage result = lastDownloads.get(ticker);
        if (result == null) {
            String fileName = String.format("file://%s/%s",getDateFeedStoreDir(),tickerFileNamer.apply(ticker));
            log.info(String.format("Restoring saved %s to HtmlPage", fileName));
            URL url = new URL(fileName);

            if (webClient == null) {
                webClient = new WebClient();
                webClient.getOptions().setJavaScriptEnabled(false);
            }

            result = webClient.getPage(url);
        }
        return result;
    }

    //endregion Interface DownloadManager

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

    public Function<Object, String> getTickerFileNamer() {
        return tickerFileNamer;
    }

    public void setTickerFileNamer(Function<Object, String> tickerFileNamer) {
        this.tickerFileNamer = tickerFileNamer;
    }

    //endregion Properties



}
