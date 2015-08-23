(ns ta-crash.handlers
  (:require
    [ta-crash.data-access :as data-access]))

(defn aggregate-data
  [aggregate-by crashes]
  crashes)

(defn get-crash-data
  ([type identifier req]
   (data-access/get-crash-data type identifier))
  ([type identifier date-range req]
   (data-access/get-crash-data-for-date-range type identifier date-range))
  ([type identifier date-range date-aggregate req]
   (aggregate-data date-aggregate (data-access/get-crash-data-for-date-range type identifier date-range))))

(defn get-shape
  ([area-type]
   (data-access/get-all-shapes area-type))
  ([area-type identifier]
   (data-access/get-shape area-type identifier)))
