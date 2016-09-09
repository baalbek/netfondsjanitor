(defproject netfondsjanitor "5.2"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
		[org.aspectj/aspectjrt "1.8.9"]
		[org.aspectj/aspectjtools "1.8.9"]
		[org.aspectj/aspectjweaver "1.8.9"]
		[org.springframework/spring-core "4.2.3.RELEASE"]
		[org.springframework/spring-context "4.2.3.RELEASE"]
		[org.springframework/spring-aop "4.2.3.RELEASE"]
		[org.clojure/clojure "1.7.0"]
		[org.clojure/clojure-contrib "1.2.0"]
		[org.mybatis/mybatis "3.4.1"]
		[junit/junit "4.11"]
		[log4j/log4j "1.2.17"]
		[args4j/args4j "2.33"]
		[org.clojure/algo.monads "0.1.5"]
		[net.sourceforge.htmlunit/htmlunit "2.22"]
		[org.jsoup/jsoup "1.8.3"]
		[org.ccil.cowan.tagsoup/tagsoup "1.2.1"]
		[colt/colt "1.2.0"]

    ]
  ;:main ^:skip-aot harborview.webapp
  ;:compile 
  :target-path "build"
  :source-paths ["src/clojure"]
  :test-paths ["test/clojure" "dist" "test/resources"]
  :java-source-paths ["src/java" "test/java"]
  :javac-options     ["-target" "1.8" "-source" "1.8"]
  :aot :all
  ;:test {:resource-paths ["test/resources" "dist"]}
  :resource-paths [
		"/home/rcs/opt/java/maunaloax/build/libs/maunaloax-5.3.1.jar"
		"/home/rcs/opt/java/netfonds-repos/build/libs/netfondsrepos-1.0.jar"
		"/home/rcs/opt/java/oahu/build/libs/oahu-5.3.1.jar"
		"/home/rcs/opt/java/oahux/build/libs/oahux-5.3.1.jar"
		"/home/rcs/opt/java/ranoraraku/build/libs/ranoraraku-5.3.5.jar"
		"/home/rcs/opt/java/waimea/build/libs/waimea-5.3.SNAPSHOT.jar"

                   ]
  :profiles {:uberjar {:aot :all}})
