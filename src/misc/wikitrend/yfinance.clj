(ns misc.wikitrend.yfinance
  (:require clj-util)

  (:require [clojure.string :as str])
  (:require [clojure.pprint :as pp])

  (:require [clj-time.core :as tmc])
  (:require [clj-time.format :as tmf])
  )

(def YMD_FORMATTER (tmf/formatters :year-month-day))

(defn read-file [filename]
  (->> (clj-util/read-lines filename)
       rest
       (map #(str/split % #","))
       (map (fn [[date _ _ _ _ _ close]] [(tmf/parse YMD_FORMATTER date) (Double. close)]))
       (into {}))
  )

(defn split-by-week [yfinance st ed]
  (loop [now st res [] c-price nil]
    (if (or (tmc/before? now ed) (= now ed))
      (if (= (tmc/day-of-week now) 7)
        (recur (tmc/plus now (tmc/days 1)) res nil)
        (let [price (yfinance now)]
          (recur (tmc/plus now (tmc/days 1)) 
                 (if (and (nil? c-price) price) (conj res [now price]) res) price))
        )
      res
      )
    )
  )

(defn -main [& args]
  (let [opts (clj-util/cli-simple args)
        st (tmf/parse YMD_FORMATTER (opts :st))
        ed (tmf/parse YMD_FORMATTER (opts :ed))
        yfinance (read-file *in*)
        ]
    (doseq [[date price] (split-by-week yfinance st ed)]
      (println (format "%4d-%02d-%02d\t%f" (tmc/year date) (tmc/month date) (tmc/day date) price))
      )
    )
  )
