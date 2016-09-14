package netfondsjanitor;

import oahu.aspects.cache.Cacheable;
import oahu.financial.janitors.Janitor;
import org.apache.log4j.PropertyConfigurator;
import org.kohsuke.args4j.CmdLineException;
import netfondsjanitor.service.CmdLineValues;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import oahu.financial.repository.StockMarketRepository;

import java.io.IOException;
import java.util.Properties;

public class App {
    public static void main(String[] args) {
        initLog4j();
        try {
            CmdLineValues opts = new CmdLineValues(args);

            ApplicationContext factory = new ClassPathXmlApplicationContext(opts.getXml());

            Janitor janitor = factory.getBean("janitor",Janitor.class);

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
