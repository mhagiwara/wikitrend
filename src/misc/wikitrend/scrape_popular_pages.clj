(ns misc.wikitrend.scrape-popular-pages
  (:require [net.cgrand.enlive-html :as enlive])
  (:require [clojure.pprint :as pp])
  (:require [clojure.string :as str])
  )

(defn fetch-url [url]
  (enlive/html-resource (java.net.URL. url)))

(defn -main [& args]
  (let [html
        (fetch-url "http://wikistics.falsikon.de/long/wikipedia/en/3000.htm")]
    (println (str/join "\n" (map #(first (:content %))
                                 (enlive/select html [:li :a :b]))))
    )
  )