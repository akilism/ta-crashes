(ns ta-crash.total-groups
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn total-item-display
  [[type val]]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "total-item"}
        (dom/p nil val)
        (dom/p nil (get-type-display type))))))

(defn total-group-item
  [total owner]
  (reify
    om/IRender
    (render [this]
      (println "total-group-item: " total)
      (om/build total-item-display total))))

(defn total-group
  [total owner]
  (reify
    om/IRender
    (render [this]
      (println "total-group: " total)
      (om/build total-display total))))

(defn total-groups-view
  [data owner]
  (reify
    om/IRender
    (render [this]
      ;(println "total-group-view: " data)
      (dom/div #js {:className "totals"}
        (apply dom/ul #js {:className "total-groups"}
          (om/build-all total-group (:totals data)))))))
