(ns example.core
  (:require [josef.consumer :as jc]
            [josef.producer :as jp])
  (:gen-class))

(defn msg->String
  [m]
  (String. (.message m)))

(defn more-than-four
  [s]
  (< 4 (count s)))

(defn produce-result!
  [p m]
  (jp/send! p "upped" (.getBytes m)
            :on-success (fn [topic offset partition]
                          (println "produced" m "to" topic))
            :on-error (fn [e]
                        (comment "handle exception"))))

(defn -main
  [& args]
  (let [prdcr (jp/producer "localhost:9092")            ;; get a Kafka producer
        cnsmr (jc/consumer "localhost:2181" "whatever") ;; get a Kafka consumer
        topic-pattern "test.*"                          ;; specify all of our test topics
        num-streams 4                                   ;; create 4 streams for parellel string uppercasing
        xf (comp                                        ;; compose our processing transducer xform
            (map msg->String)                           ;; stringify each message
            (filter more-than-four)                     ;; only process stings that are longer than four characters
            (map clojure.string/upper-case)             ;; uppercase the strings
            (partition-all 5))                          ;; and batch together 5 at a time
        rf #(doseq [m %]                                ;; doseq across our batch of 5 
              (produce-result! prdcr m))]               ;; and produce to the "upped" topic
    (jc/process-streams! cnsmr topic-pattern num-streams xf rf))) ;; wrap the whole thing up into a single call
