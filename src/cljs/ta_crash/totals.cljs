(ns ta-crash.total-groups
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn get-title-display
  [{:keys [type]} :as total]
  (cond
    (= type :crashes) (str "Total Crashes " (:total-crashes total))
    (= type :injured) (str "Total Injuries " (:people-injured total))
    (= type :killed) (str "Total Deaths " (:people-killed total))))

(get-type-display
  [type]
  (cond
    (= type :total-with-injured) (str "Injury")
    (= type :total-with-death) (str "Death")
    (or (= type :pedestrians-injured) (= type :pedestrians-killed)) (dom/i #js {:className "icon-pedestrians"})
    (or (= type :bicyclists-injured) (= type :bicyclists-killed)) (dom/i #js {:className "icon-bicyclists"})
    (or (= type :motorists-injured) (= type :motorists-killed)) (dom/i #js {:className "icon-motorists"})))


; ~~~~~~~~~~~~~~~~~~~~~~~~~
; Total Display Components
; ~~~~~~~~~~~~~~~~~~~~~~~~~

(defn total-item-display
  [[type val]]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "total-item"}
        (dom/p #js {:className "total-item-count"} val)
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
      (om/dom section nil
        (om/dom p #js {:className "total-title"}
          (get-title-display total))
        (apply dom/ul #js {:className "total-group-items"}
          (om/build-all total-group-item (total)))))))

(defn total-groups-view
  [data owner]
  (reify
    om/IRender
    (render [this]
      ;(println "total-group-view: " data)
      (dom/div #js {:className "totals"}
        (apply dom/ul #js {:className "total-groups"}
          (om/build-all total-group (:totals data)))))))
