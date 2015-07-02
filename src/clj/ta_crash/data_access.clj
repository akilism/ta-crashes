(ns ta-crash.data-access
  (:require
    [clojure.string :refer [split]]))


;; a data point
; {:type "precinct"
;  :identifier "83"
;  :year 2015
;  :month 0
;  :total-crashes 103
;  :total-with-injured 76
;  :total-with-death 4
;  :people-injured 98
;  :bicyclists-injured 10
;  :pedestrians-injured 58
;  :motorists-injured 30
;  :people-killed 5
;  :bicyclists-killed 1
;  :pedestrians-killed 3
;  :motorists-killed 1
;  :contributing-factors [[:lost-consciousness 10]
;                         [:oversized-vehicle 4]
;                         [:driver-inexperience 21]
;                         [:prescription-medication 11]
;                         [:unspecified 83]
;                         [:fatigued-drowsy 32]
;                         [:backing-unsafely 32]
;                         [:driver-inattention 64]]
;  :vehicle-types [[:passenger-vehicle 93]
;                  [:unknown 83]
;                  [:van 30]
;                  [:truck 19]
;                  [:bicycle 10]
;                  [:sport-utility 43]]}

(defn build-point
  [year month]
  {:type "precinct"
   :identifier "83"
   :year year
   :month month
   :total-crashes (rand-int 200)
   :total-with-injured (rand-int 101)
   :total-with-death (rand-int 13)
   :people-injured (rand-int 101)
   :bicyclists-injured (rand-int 25)
   :pedestrians-injured (rand-int 25)
   :motorists-injured (rand-int 25)
   :people-killed(rand-int 13)
   :bicyclists-killed (rand-int 8)
   :pedestrians-killed (rand-int 8)
   :motorists-killed (rand-int 8)
   :contributing-factors [[:lost-consciousness (rand-int 95)]
                          [:oversized-vehicle (rand-int 95)]
                          [:driver-inexperience (rand-int 95)]
                          [:prescription-medication (rand-int 95)]
                          [:unspecified (rand-int 95)]
                          [:fatigued-drowsy (rand-int 95)]
                          [:backing-unsafely (rand-int 95)]
                          [:driver-inattention (rand-int 95)]]
   :vehicle-types [[:passenger-vehicle (rand-int 60)]
                   [:unknown (rand-int 60)]
                   [:van (rand-int 60)]
                   [:truck (rand-int 60)]
                   [:bicycle (rand-int 60)]
                   [:sport-utility (rand-int 60)]]})

(defn generate-fake-data
  [years]
  (for [year years
        month (range 12)
        :let [data-point (build-point year month)]]
    data-point))

(def crash-data (generate-fake-data [2015]))

(defn get-date-values
  [date-range]
  (split date-range #"!$!"))

(defn get-crash-data
  [type identifier]
  ;; query database here.
  crash-data)

(defn get-crash-data-for-date-range
  [type identifier date-range]
  (let [[start end] (get-date-values date-range)]
    ;; query database here.
    crash-data))
