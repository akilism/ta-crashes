(ns ta-crash.data-access
  (:require
    [clojure.string refer [split]]))

(def crashes [])

(defn get-date-values
  [date-range]
  (split date-range #"!$!"))

(defn get-crash-data
  [type identifier]
  crashes)

(defn get-crash-data-for-date-range
  [type identifier date-range]
  (let [[start end] (get-date-values date-range)]
    ;; query database here.
    crashes))
