(ns josef.producer
  (:require [clj-kafka.producer :as p]
            [clj-kafka.zk :as zk])
  (:require [zookeeper :as z])
  (:require [clojure.walk :refer [postwalk]]
            [clojure.string :as s]))

(defn keyword-to-property
  "Replace :foo-bar with \"foo.bar\", etc."
  [k]
  (s/replace (name k) #"\-" "."))

(defn propertyize-map
  "Recursively transforms all map keys from keywords to Java property strings 
  and values to strings (if necessary)."
  [m]
  (let [f (fn [[k v]]
            (let [k (keyword-to-property k)]
              (cond
                (string? v) [k v]
                (keyword? v) [k (name v)]
                :else [k (str v)])))]
    (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn broker-lookup
  "Takes the arg map from producer and looks up the Kafka brokers from 
  zookeeper or just returns the given direct list of Kafka brokers."
  [args]
  (if-let [z (:zookeeper-connect args)]
     (zk/broker-list (zk/brokers {"zookeeper.connect" z}))
     (:metadata-broker-list args)))

(defn producer
  [topic & {:keys [zookeeper-connect
                   metadata-broker-list
                   serializer-class
                   partitioner-class]
            :as args
            :or {metadata-broker-list "localhost:9092"
                 serializer-class "kafka.serializer.DefaultEncoder"
                 partitioner-class "kafka.producer.DefaultPartitioner"}}]
  (let [broker-list (broker-lookup args)
        producer-props (dissoc
                        (merge args {"metadata.broker.list" broker-list})
                        :zookeeper-connect)]
    {:producer (p/producer (propertyize-map producer-props))
     :topic topic}))

(defn as-message
  [topic m]
  (apply p/message
         topic
         (if (map? m)
           (map #(.getBytes %) (first (into [] m)))
           [nil (.getBytes m)])))

(defn send
  [p & ms]
  (let [topic (:topic p)
        msgs (map #(as-message topic %) ms)]
    (p/send-messages (:producer p) msgs)))


                  
                     


