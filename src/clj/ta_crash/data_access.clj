(ns ta-crash.data-access
  (:require
    [clojure.string :as str]
    [clojure.data.json :as json]))

(defn get-borough-id
  [borough]
  (case borough
    "bronx" 2
    "brooklyn" 3
    "manhattan" 1
    "queens" 4
    "staten-island" 5))

(defn build-point
  [year month]
  {:type "precinct"
   :identifier "83"
   :year year
   :month month
   :totals [{:type :crashes
             :total-crashes (rand-int 200)
             :total-with-injured (rand-int 101)
             :total-with-death (rand-int 13)}
            {:type :injured
             :people-injured (rand-int 101)
             :bicyclists-injured (rand-int 25)
             :pedestrians-injured (rand-int 25)
             :motorists-injured (rand-int 25)}
            {:type :killed
             :people-killed (rand-int 13)
             :bicyclists-killed (rand-int 8)
             :pedestrians-killed (rand-int 8)
             :motorists-killed (rand-int 8)}
            {:type :contributing-factors
             :lost-consciousness (rand-int 95)
             :oversized-vehicle (rand-int 95)
             :driver-inexperience (rand-int 95)
             :prescription-medication (rand-int 95)
             :unspecified (rand-int 95)
             :fatigued-drowsy (rand-int 95)
             :backing-unsafely (rand-int 95)
             :driver-inattention (rand-int 95)}
            {:type :vehicle-types
             :passenger-vehicle (rand-int 60)
             :unknown (rand-int 60)
             :van (rand-int 60)
             :truck (rand-int 60)
             :bicycle (rand-int 60)
             :sport-utility (rand-int 60)}]})

(defn generate-fake-data
  [years]
  (for [year years
        month (range 12)
        :let [data-point (build-point year month)]]
    data-point))

(def crash-data (generate-fake-data [2015]))

(defn get-date-values
  [date-range]
  (str/split date-range #"!$!"))

(defn get-crash-data
  [type identifier]
  ;; query database here.
  crash-data)

(defn get-crash-data-for-date-range
  [type identifier date-range]
  (let [[start end] (get-date-values date-range)]
    ;; query database here.
    crash-data))

(defn get-all-shapes
  [area-type]
  (json/read-str (slurp (str "static-resources/geo/" area-type ".geojson")) :key-fn keyword))

(defn get-geo-features
  [identifier features]
  (filter #(= identifier (get-in % [:properties :identifier])) features))

(defn get-shape
  [area-type identifier]
  ;(println area-type ":" identifier)
  (let [data (get-all-shapes area-type)
        features (:features data)]
    ;(println (get-in (first features) [:properties :identifier]) " - " (get-borough-id identifier))
    (if (= "borough" area-type)
      {:type "FeatureCollection" :features (get-geo-features (get-borough-id identifier) features)}
      {:type "FeatureCollection" :features (get-geo-features identifier features)})))
