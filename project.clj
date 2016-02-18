(defproject netfondsjanitor "5.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;:dependencies [[enlive "1.1.6"]
  ;								 [compojure "1.4.0"]
  ;								 [clj-json "0.5.3"]
  ;								 [ring/ring-core "1.4.0"]
  ;								 [ring/ring-jetty-adapter "1.4.0"]
  ;								 [ring/ring-servlet "1.4.0"]
  ;								 ]
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
    ;deps
    		"src/resources"
		    "/home/rcs/opt/java/oahu/build/libs/oahu-5.1.5.jar"
		    "/home/rcs/opt/java/maunakea/build/libs/maunakea-5.1.2.jar"
		    "/home/rcs/opt/java/vega/build/libs/vegaq-5.1.1.jar"
		    "/home/rcs/opt/java/ranoraraku/build/libs/ranoraraku-5.1.5.jar"
		    "libs/org.aspectj/aspectjrt/1.8.8/7c5b26f24375685e34a50c2d765ebc40a96a5280/aspectjrt-1.8.8.jar"
		    "libs/org.aspectj/aspectjtools/1.8.8/7c6527b530b0289d36163f82f04db20b3be838c5/aspectjtools-1.8.8.jar"
		    "libs/org.aspectj/aspectjweaver/1.8.8/969c8d3d3d56db1e1c8721a03ef07d0c9ac3a2e4/aspectjweaver-1.8.8.jar"
		    "libs/org.springframework/spring-core/4.2.3.RELEASE/3ed00dad7a16b2a28df9348294f6a67151f43cf6/spring-core-4.2.3.RELEASE.jar"
		    "libs/org.springframework/spring-context/4.2.3.RELEASE/fb26a160d984eb9cbf01fbf38f4b17e15fdd50e1/spring-context-4.2.3.RELEASE.jar"
		    "libs/org.springframework/spring-aop/4.2.3.RELEASE/6384a57a024df03bfcb1eeccd3d820da98125b92/spring-aop-4.2.3.RELEASE.jar"
		    "libs/junit/junit/4.11/4e031bb61df09069aeb2bffb4019e7a5034a4ee0/junit-4.11.jar"
		    "libs/log4j/log4j/1.2.17/5af35056b4d257e4b64b9e8069c0746e8b08629f/log4j-1.2.17.jar"
		    "libs/args4j/args4j/2.0.28/bd0cbe2595ea7599a9fdcf08f5dad1b911d746e5/args4j-2.0.28.jar"
		    "libs/org.clojure/algo.monads/0.1.5/bc4c31f3b08eb8bed1c57e9ed786b3448c3c325b/algo.monads-0.1.5.jar"
		    "libs/net.sourceforge.htmlunit/htmlunit/2.18/ec30f76601f7010abbc09cc8ec401cb183916371/htmlunit-2.18.jar"
		    "libs/org.mybatis/mybatis/3.3.0/7c0e3582a1518b6d8e4e37ce2fc49b7bb6f2040f/mybatis-3.3.0.jar"
		    "libs/org.postgresql/postgresql/9.4-1206-jdbc41/4ef3d9bcf654c24783aa50cda10d29e1a9d40711/postgresql-9.4-1206-jdbc41.jar"
		    "libs/commons-lang/commons-lang/20030203.000129/57892c55a786ab9d407ba31c01906b61184933ed/commons-lang-20030203.000129.jar"
		    "libs/org.clojure/clojure/1.7.0/4953eb1ffa4adca22760c9324c9c26d2038c392a/clojure-1.7.0.jar"
		    "libs/commons-logging/commons-logging/1.2/4bfc12adfe4842bf07b657f0369c4cb522955686/commons-logging-1.2.jar"
		    "libs/org.springframework/spring-beans/4.2.3.RELEASE/249921ead97248410ac04f854075f0a1c1a567e4/spring-beans-4.2.3.RELEASE.jar"
		    "libs/org.springframework/spring-expression/4.2.3.RELEASE/67b064c726c276436387c7a183a49deda153a6cc/spring-expression-4.2.3.RELEASE.jar"
		    "libs/aopalliance/aopalliance/1.0/235ba8b489512805ac13a8f9ea77a1ca5ebe3e8/aopalliance-1.0.jar"
		    "libs/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar"
		    "libs/org.clojure/tools.macro/0.1.0/2b2d0caacbe47b3befa9e50e9a6dd27d4ad784e5/tools.macro-0.1.0.jar"
		    "libs/xalan/xalan/2.7.2/d55d3f02a56ec4c25695fe67e1334ff8c2ecea23/xalan-2.7.2.jar"
		    "libs/commons-collections/commons-collections/3.2.1/761ea405b9b37ced573d2df0d1e3a4e0f9edc668/commons-collections-3.2.1.jar"
		    "libs/org.apache.commons/commons-lang3/3.4/5fe28b9518e58819180a43a850fbc0dd24b7c050/commons-lang3-3.4.jar"
		    "libs/org.apache.httpcomponents/httpclient/4.5/a1e6cbb3cc2c5f210dd1310ff9fcb2c09c0d1438/httpclient-4.5.jar"
		    "libs/org.apache.httpcomponents/httpmime/4.5/7bdb321e86724b16af6134a0fd22fec649eda971/httpmime-4.5.jar"
		    "libs/commons-codec/commons-codec/1.10/4b95f4897fa13f2cd904aee711aeafc0c5295cd8/commons-codec-1.10.jar"
		    "libs/net.sourceforge.htmlunit/htmlunit-core-js/2.17/4316d68f449d42f69faf4ee255aa31b03e4f7dd5/htmlunit-core-js-2.17.jar"
		    "libs/xerces/xercesImpl/2.11.0/9bb329db1cfc4e22462c9d6b43a8432f5850e92c/xercesImpl-2.11.0.jar"
		    "libs/net.sourceforge.nekohtml/nekohtml/1.9.22/4f54af68ecb345f2453fb6884672ad08414154e3/nekohtml-1.9.22.jar"
		    "libs/net.sourceforge.cssparser/cssparser/0.9.16/3f751904d467537b8ee99c612e69d4e79d6271cf/cssparser-0.9.16.jar"
		    "libs/commons-io/commons-io/2.4/b1b6ea3b7e4aa4f492509a4952029cd8e48019ad/commons-io-2.4.jar"
		    "libs/org.eclipse.jetty.websocket/websocket-client/9.2.12.v20150709/79e439a426514af24e693c78662cb2b4db2be3c6/websocket-client-9.2.12.v20150709.jar"
		    "libs/xalan/serializer/2.7.2/24247f3bb052ee068971393bdb83e04512bb1c3c/serializer-2.7.2.jar"
		    "libs/org.apache.httpcomponents/httpcore/4.4.1/f5aa318bda4c6c8d688c9d00b90681dcd82ce636/httpcore-4.4.1.jar"
		    "libs/xml-apis/xml-apis/1.4.01/3789d9fada2d3d458c4ba2de349d48780f381ee3/xml-apis-1.4.01.jar"
		    "libs/org.w3c.css/sac/1.3/cdb2dcb4e22b83d6b32b93095f644c3462739e82/sac-1.3.jar"
		    "libs/org.eclipse.jetty/jetty-util/9.2.12.v20150709/d99d38adfdb5ec677643f04fa862554b0bb8b42e/jetty-util-9.2.12.v20150709.jar"
		    "libs/org.eclipse.jetty/jetty-io/9.2.12.v20150709/c02e9e303d231a589e0c8866c1ee89bcdeb40a55/jetty-io-9.2.12.v20150709.jar"
		    "libs/org.eclipse.jetty.websocket/websocket-common/9.2.12.v20150709/97219331c7ec90b2964b3a50d86008e1f16f5638/websocket-common-9.2.12.v20150709.jar"
		    "libs/org.eclipse.jetty.websocket/websocket-api/9.2.12.v20150709/6bb55e8ef1a385e40f144a240492915b89914cfb/websocket-api-9.2.12.v20150709.jar"
    ;deps
                   ]
  :profiles {:uberjar {:aot :all}})
