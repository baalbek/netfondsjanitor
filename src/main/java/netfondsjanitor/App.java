package netfondsjanitor;

import oahu.financial.janitors.Janitor;
import org.apache.log4j.PropertyConfigurator;
import org.kohsuke.args4j.CmdLineException;
import netfondsjanitor.service.CmdLineValues;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Properties;

public class App {
    public static void main(String[] args) {
        initLog4j();
        try {
            CmdLineValues opts = new CmdLineValues(args);

            ApplicationContext factory = new ClassPathXmlApplicationContext(opts.getXml());

            Janitor janitor = factory.getBean("janitor",Janitor.class);

            /*List<String> tickers = opts.getTickers();
            if (tickers == null) {
                Locator locator = factory.getBean("locator",Locator.class);
                List<Stock> stox = locator.getTickers();
                tickers = stox.stream().map(Stock::getTicker).collect(Collectors.toList());
            }*/

            janitor.run(opts);

        } catch (CmdLineException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void initLog4j() {
        Properties props = new Properties();
        try {
            props.load(App.class.getResourceAsStream("/log4j.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PropertyConfigurator.configure(props);
    }
}
