(ns misc.wikitrend.align
  (:require clj-util)

  (:require [clojure.string :as str])
  (:require [clojure.pprint :as pp])

  (:require [clj-time.core :as tmc])
  (:require [clj-time.format :as tmf])
  )

(defn read-aligned-file [filename]
  (->> (clj-util/read-lines filename)
       (map #(str/split % #"\t"))
       (map (fn [[_ view date price]]
              (if (and price view)
                [date
                 (if (= view "null") nil (Integer. view))
                 (Double. price)])))
       (filter identity)
       )
  )

(defn -main [& args]
  (let [aligned (read-aligned-file *in*)
        aligned-batch (partition-all 6 1 aligned)]
    (doseq [batch aligned-batch]
      (let [[[_ v-3 _] [_ v-2 _] [_ v-1 _] [date v _]
             [_ _ p+1] [_ _ p+2]] batch]
        (if (and v-3 v-2 v-1 v p+1 p+2)
          (let [v-avr (/ (+ v-3 v-2 v-1) 3)
                v-delta (float (/ v v-avr))
                p-delta (float (/ p+2 p+1))]
            (println (str date "\t" v-delta "\t" p-delta))
            )
          )
        )
      )
    )
  )