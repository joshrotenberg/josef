(ns josef.util
  (:import [java.util Properties])
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

(defn map->Properties
  ""
  [m]
  (let [p (propertyize-map m)
        props (Properties.)]
    (doseq [[k v] p]
      (.setProperty props k v))
    props))
