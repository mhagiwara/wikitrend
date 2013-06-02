(ns misc.wikitrend.crawl-grok-stats
  (:require clj-util)

  (:require [clojure.java.io :as io])
  (:require [clojure.string :as str])
  (:require [clojure.data.json :as json])
  (:require [clojure.pprint :as pp])
  (:require [clojure.tools.logging :as logging])

  (:require [clj-time.core :as tmc])
  (:require [clj-time.format :as tmf])
  )

(defn crawl-month [title now]
  (let [year (tmc/year now)
        month (tmc/month now)
        title-enc (str/replace (java.net.URLEncoder/encode title) "+" "%20")
        url (format "http://stats.grok.se/json/en/%d%02d/%s"
                          year month title-enc)]
    (logging/info "crawl-month: " url)
    (->> url
         clj-util/read-lines
         (str/join "\n")
         json/read-str
         )
    )
  )

(defn crawl-period [title st ed]
  (loop [now st res {}]
    (if (or (tmc/before? now ed) (= now ed))
      (do
        (Thread/sleep 1000)
        (recur (tmc/plus now (tmc/months 1))
               (merge res ((crawl-month title now) "daily_views")))
        )
      res
      )
    )
  )

(defn -main [& args]
  (let [opts (clj-util/cli-simple args)
        formatter (tmf/formatters :year-month-day)
        res (crawl-period
             (opts :title)
             (tmf/parse formatter (opts :st))
             (tmf/parse formatter (opts :ed)))]
     (doseq [[k v] (sort res)]
       (println (str k "\t" v))
       )
     )
  )
