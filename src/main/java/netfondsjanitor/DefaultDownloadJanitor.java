package netfondsjanitor;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import oahu.exceptions.NotImplementedException;
import oahu.financial.DownloaderJanitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.joda.time.DateTime;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 4/18/13
 * Time: 11:12 AM
 */
public class DefaultDownloadJanitor implements DownloaderJanitor {
    private String storePath;

    public DefaultDownloadJanitor() {

    }

    @Override
    public void storeLoginPage(HtmlPage htmlPage) {
        throw new NotImplementedException();
    }

    @Override
    public void storeLogoutPage(HtmlPage htmlPage) {
        throw new NotImplementedException();
    }

    @Override
    public void storeDerivativePage(HtmlPage htmlPage, String s) {
        throw new NotImplementedException();
    }

    @Override
    public void storeIndexPage(HtmlPage page, String ticker) {

        DateTime dt = new DateTime();
        int year = dt.year().get();
        int month = dt.monthOfYear().get();
        int day = dt.dayOfMonth().get();

        File out = new File(String.format("%s/%s-%d-%d-%d.html", getStorePath(), ticker, year, month, day));
        try (FileOutputStream fop = new FileOutputStream(out)) {

            // if file doesn't exists, then create it
            if (!out.exists()) {
                out.createNewFile();
            }

            // get the content in bytes

            byte[] contentInBytes = page.asXml().getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }
}
