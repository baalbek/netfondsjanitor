package netfondsjanitor.etrade;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import oahu.exceptions.NotImplementedException;
import oahu.financial.html.EtradeDownloader;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 4/20/13
 * Time: 9:55 PM
 */
public class DummyDownloader implements EtradeDownloader {
    private String storePath;
    private String indexSuffix = null;
    private String optionSuffix = null;
    private Logger log = Logger.getLogger(getClass().getPackage().getName());


    private URL getUrl(String ticker, String curSuffix) throws MalformedURLException {
        String urlStr = curSuffix == null ?
                                String.format("file://%s/%s.html",getStorePath(),ticker) :
                                String.format("file://%s/%s-%s.html",getStorePath(),ticker, curSuffix);
        log.info("Url string: " + urlStr);
        URL url = new URL(urlStr);
        return url;
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public String getOptionSuffix() {
        return optionSuffix;
    }

    public void setOptionSuffix(String optionSuffix) {
        this.optionSuffix = optionSuffix;
    }



    @Override
    public Page downloadDerivatives(String ticker) throws IOException {
        URL url = getUrl(ticker,getOptionSuffix());

        System.out.println("URL: " + url.toString());

        WebClient webClient = new WebClient();

        return webClient.getPage(url);
    }

    @Override
    public Page downloadIndex(String stockIndex) throws IOException {
        URL url = getUrl(stockIndex,getIndexSuffix());

        WebClient webClient = new WebClient();

        return webClient.getPage(url);
    }

    @Override
    public Page downloadPaperHistory(String ticker) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void login() throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void logout() throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void closeAllWindows() {
        throw new NotImplementedException();
    }

    @Override
    public Page getLoginPage() {
        throw new NotImplementedException();
    }

    @Override
    public Page getLogoutPage() {
        throw new NotImplementedException();
    }

    public String getIndexSuffix() {
        return indexSuffix;
    }

    public void setIndexSuffix(String indexSuffix) {
        this.indexSuffix = indexSuffix;
    }
}
