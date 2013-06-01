(ns misc.wikitrend.crawl-pagecount
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as str])
  (:import java.util.zip.GZIPInputStream
           java.util.zip.GZIPOutputStream)
  (:require [clojure.pprint :as pp])
  (:require [clojure.tools.logging :as logging])
  )

(defn date->calendar [d]
  (doto (java.util.Calendar/getInstance)
        (.setTime d) (.setTimeZone (java.util.TimeZone/getTimeZone "UTC"))))

;; date-> java.util.Date
(defn date->urls [date]
  (for [hour (range 0 24)]
    (let [cal (date->calendar date)
          year (.get cal java.util.Calendar/YEAR)
          month (.get cal java.util.Calendar/MONTH)
          day  (.get cal java.util.Calendar/DATE)]
      (format "http://dumps.wikimedia.org/other/pagecounts-raw/%d/%d-%02d/pagecounts-%d%02d%02d-%02d0000.gz"
              year year month year month day hour)
      )
    )
  )

(defn crawl-hour [url]
  (logging/info "crawl-hour: " url)
  (->>
   (-> url                    
       io/input-stream GZIPInputStream.
       io/reader
       line-seq)
   (filter #(re-seq #"^en " %) )
   (map #(let [[url pv _] (str/split (subs % 3) #" ")]
           [url (Integer. pv)]) )
   (into {})
   )
  )

(defn crawl-day [date]
  (->> date
       date->urls
       (pmap crawl-hour)
       (apply merge-with +))
  )

(defn -main [& args]
  (pp/pprint (crawl-day #inst"2013-05-01"))
;;  (let [urls (date->urls #inst"2013-05-01")]
    ;; (pp/pprint (sort-by (comp - second) (crawl-hour (first urls))))
;;    )
  )
