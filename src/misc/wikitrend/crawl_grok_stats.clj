(ns misc.wikitrend.crawl-grok-stats
  (:require clj-util)

  (:require [clojure.java.io :as io])
  (:require [clojure.string :as str])
  (:require [clojure.data.json :as json])
  )

(defn crawl-month [title date]
  (->> "http://stats.grok.se/json/en/201305/Apple%20Inc."
       clj-util/read-lines
       (str/join "\n")
       json/read-str
       )
  )

(defn -main [& args]
  (crawl-month "Apple Inc." #"2013-05-01")
  )