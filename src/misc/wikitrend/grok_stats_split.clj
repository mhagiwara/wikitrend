(ns misc.wikitrend.grok-stats-split
  (:require clj-util)

  (:require [clojure.string :as str])
  (:require [clojure.pprint :as pp])

  (:require [clj-time.core :as tmc])
  (:require [clj-time.format :as tmf])
  )

(def YMD_FORMATTER (tmf/formatters :year-month-day))

(defn read-file [filename]
  (->> (clj-util/read-lines filename)
       (map #(str/split % #"\t"))
       (map (fn [[date views]] [(try (tmf/parse YMD_FORMATTER date)
                                     (catch Exception e nil))
                                (Integer. views)]))
       (filter #(first %))
       (into {}))
  )

(defn split-by-week [grok-stats st ed]
  (loop [now st res [] weekly 0]
    (if (or (tmc/before? now ed) (= now ed))
      (let [view (grok-stats now)
            weekly (if (and weekly view (not= view 0)) (+ weekly view) nil)]
        (if (= (tmc/day-of-week now) 7)
          (let [date-monday (tmc/minus now (tmc/days 6))]
            (recur (tmc/plus now (tmc/days 1))
                   (conj res [date-monday weekly]) 0))
          (recur (tmc/plus now (tmc/days 1)) res weekly)

          )
        )
       res
       )
     )
   )

(defn -main [& args]
  (let [opts (clj-util/cli-simple args)
        st (tmf/parse YMD_FORMATTER (opts :st))
        ed (tmf/parse YMD_FORMATTER (opts :ed))
        grok-stats (read-file *in*)
        ]
    (doseq [[date views] (split-by-week grok-stats st ed)]
      (println (format "%4d-%02d-%02d\t%s" (tmc/year date) (tmc/month date) (tmc/day date) views))
      )
    )
  )