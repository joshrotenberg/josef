# josef

Boilerplate reduction for processing Kafka streams with Clojure Transducers

## Usage

```clojure
(ns my.app
    (:require [josef.consumer :as c]))

(defn msg->String
      [m]
     (String. (.message m)))


(defn -main
      [& args]
      (let [cons (c/get-consumer "localhost:2181" "whatever")]
      	   (c/process-stream con "test"
	   		     (comp (map msg->String)                # turn kafka message payload into a string
                                   (filter #(< 4 (count %)))        # filter out strings shorter than 4 characters
			           (map clojure.string/upper-case)) # uppercase them 
			      println)))                            # print to stdout
```

## Overview

`josef` is a simple wrapper around
[clj-kafka](https://github.com/pingles/clj-kafka) to make processing
Kafka streams with Clojure
[transducers](http://clojure.org/transducers) easier.

I had a bunch of similar but ultimately distinct filters that needed
to process the same Kafka topic. Clojure
transducers are a great way to
compose various pieces and build transformations and filters, but I
found myself writing the same boilerplate otherwise. After two, I
broke it out into a seprate, more general function. After a few more,
I figured it was time for a little library.

## License

Copyright © 2015 Josh Rotenberg

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
