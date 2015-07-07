(ns ta-crash.total-groups
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ta-crash.aggregation :as agg]
            [ta-crash.total-factors :as factor]
            [ta-crash.bar-graph :as bar-graph]))

(defmulti get-total-items (fn [type _] type))

(defmethod get-total-items :crashes
  [type items]
  (dissoc items :total-crashes :type))

(defmethod get-total-items :injured
  [type items]
  (dissoc items :people-injured :type))

(defmethod get-total-items :killed
  [type items]
  (dissoc items :people-killed :type))

(defn is-group?
  [{:keys [type]}]
  (let [non-groups [:contributing-factors :vehicle-types]]
    (not-any? #(= % type) non-groups)))

(defn is-not-group?
  [{:keys [type]}]
  (let [non-groups [:contributing-factors :vehicle-types]]
    (not (not-any? #(= % type) non-groups))))

(defn get-title-display
  [type total]
  (cond
    (= type :crashes) (str "Total Crashes " (:total-crashes total))
    (= type :injured) (str "Total Injuries " (:people-injured total))
    (= type :killed) (str "Total Deaths " (:people-killed total))))

(defn get-type-display
  [type]
  (cond
    (= type :total-with-injured) (str "Injury")
    (= type :total-with-death) (str "Death")
    (or (= type :pedestrians-injured) (= type :pedestrians-killed)) (dom/span #js {:className "icon-pedestrians"} "Pedestrians")
    (or (= type :bicyclists-injured) (= type :bicyclists-killed)) (dom/span #js {:className "icon-bicyclists"} "Bicyclists")
    (or (= type :motorists-injured) (= type :motorists-killed)) (dom/span #js {:className "icon-motorists"} "Motorists")))

; ~~~~~~~~~~~~~~~~~~~~~~~~
; Total Display Components
; ~~~~~~~~~~~~~~~~~~~~~~~~

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
      ; (println "total-group-item: " total)
      (om/build total-item-display total))))

(defn total-group
  [total owner]
  (reify
    om/IRender
    (render [this]
      (let [type (:type total)
            items (get-total-items type total)]
        ; (println "total-group: " total)
        (dom/li #js {:className "total-group"}
          (dom/p #js {:className "total-title"}
            (get-title-display type total))
          (apply dom/ul #js {:className "total-group-items"}
          (om/build-all total-group-item items)))))))

(defn total-groups-view
  [{:keys [data], :as all-data} owner]
  (reify
    om/IInitState
    (init-state [this]
      (let [totals (:totals (first (agg/total-data agg/sum-all-data data)))]
        (assoc all-data :totals totals)))
    om/IRenderState
    (render-state [this state]
      (dom/div nil (om/build bar-graph/bar-graph-view (:bar-data state)))
      ;(apply dom/div #js {:className "totals"}
      ;  (apply dom/ul #js {:className "total-groups"}
      ;    (om/build-all total-group (filter is-group? (:totals state))))
      ;  (om/build-all factor/total-factors (filter is-not-group? (:totals state))))
      )))
