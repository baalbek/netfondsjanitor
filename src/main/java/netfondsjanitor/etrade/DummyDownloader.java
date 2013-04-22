package netfondsjanitor.etrade;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import oahu.exceptions.NotImplementedException;
import oahu.financial.EtradeDownloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 4/20/13
 * Time: 9:55 PM
 */
public class DummyDownloader implements EtradeDownloader{
    private String storePath;
    private String suffix;



    private URL getUrl(String ticker) throws MalformedURLException {
        URL url = new URL("file://" + getStorePath() + "/" + ticker + "-" + getSuffix() + ".html");
        return url;
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }



    @Override
    public Page downloadDerivatives(String ticker) throws IOException {
        URL url = getUrl(ticker);

        System.out.println("URL: " + url.toString());

        WebClient webClient = new WebClient();

        return webClient.getPage(url);
    }

    @Override
    public Page downloadIndex(String stockIndex) throws IOException {
        URL url = getUrl(stockIndex);

        WebClient webClient = new WebClient();

        return webClient.getPage(url);
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
}
