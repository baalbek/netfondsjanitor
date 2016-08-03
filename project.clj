(defproject netfondsjanitor "5.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.aspectj/aspectjrt "1.8.9"]
                 [org.aspectj/aspectjweaver "1.8.9"]
                 [org.aspectj/aspectjweaver "1.8.9"]
                 [org.springframework/spring-core "4.3.2.RELEASE"]
                 [org.springframework/spring-context "4.3.2.RELEASE"]
                 [org.springframework/spring-aop "4.3.2.RELEASE"]
                 [org.clojure/clojure "1.7.0"]
                 [org.apache.logging.log4j/log4j-core "2.6.2"]
                 [args4j/args4j "2.33"]
                 [org.clojure/algo.monads "0.1.5"]
                 [net.sourceforge.htmlunit/htmlunit "2.23"]
                 [org.jsoup/jsoup "1.9.2"]
                 [org.ccil.cowan.tagsoup/tagsoup "1.2.1"]
    ]
  ;:main ^:skip-aot harborview.webapp
  ;:compile 
  :target-path "target"
  :source-paths ["src/clojure"]
  :test-paths ["test/clojure" "dist" "test/resources"]
  :java-source-paths ["src/java" "test/java"]
  :javac-options     ["-target" "1.8" "-source" "1.8"]
  :aot :all
  ;:test {:resource-paths ["test/resources" "dist"]}
  :resource-paths [
		    "../oahu/build/libs/oahu-5.3.1.jar"
		    "../ranoraraku/build/libs/ranoraraku-5.3.5.jar"
		    "../netfonds-repos/build/libs/netfondsrepos-1.0.jar"
		    "../waimea/build/libs/waimea-5.3-SNAPSHOT.jar"
                   ]
  :profiles {:uberjar {:aot :all}})
