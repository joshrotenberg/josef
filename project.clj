(defproject josef "0.1.1"
  :description "Transducers-based Kafka Streaming"
  :url "https://github.com/joshrotenberg/josef"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.apache.kafka/kafka_2.10 "0.8.2.2"]]
  :exclusions [javax.jms/jms
               com.sun.jdmk/jmxtools
               com.sun.jmx/jmxri])
