(ns ta-crash.text-stats
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]))

(def valid-types {:motorists-injured 1 :motorists-killed 1 :pedestrians-injured 1 :pedestrians-killed 1 :bicyclists-injured 1 :bicyclists-killed 1})

(defn get-type
  [t]
  (case t
    :motorists-injured :motorist
    :motorists-killed :motorist
    :pedestrians-injured :pedestrian
    :pedestrians-killed :pedestrian
    :bicyclists-injured :bicyclist
    :bicyclists-killed :bicyclist))

(defn get-break-down
  [data]
  (map (fn [[k v]] {:type (get-type k) :total v})
       (filter (fn [[k v]] (contains? valid-types k)) data)))

(defmulti build-stat
  (fn [type _]
    type))

(defmethod build-stat :injuries
  [_ data]
  {:crash-total (get-in data [:crashes :total-with-injured])
   :type-total (get-in data [:injured :people-injured])
   :crash-label " Total crashes resulting in an injury"
   :type-label " Total persons injured"
   :crash-type [:crashes :total-with-injured]
   :type-type [:injured :people-injured]
   :break-down (get-break-down (:injured data))})

(defmethod build-stat :deaths
  [_ data]
  {:crash-total (get-in data [:crashes :total-with-death])
   :type-total (get-in data [:killed :people-killed])
   :crash-label " Total crashes resulting in a death"
   :type-label " Total persons killed"
   :crash-type [:crashes :total-with-death]
   :type-type [:killed :people-killed]
   :break-down (get-break-down (:killed data))})

(defn stat-link
  [data owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [set-line-chart-data]}]
      (dom/a #js {:onClick (fn [e]
                              (.preventDefault e)
                              (put! set-line-chart-data (:type data)))
                  :href "#"}
        (dom/span #js {:className "total"} (:total data))
        (dom/span #js {:className "label"} (:label data))))))

(defn stat-break-down-view
  [data owner]
  (reify
    om/IRenderState
      (render-state [_ state]
        (dom/li nil
          (om/build stat-link
            {:total (:total data) :label (name (:type data)) :type (:type data)}
            {:init-state state})))))

(defn stat-col-view
  [data owner]
  (reify
    om/IRenderState
      (render-state [_ state]
        (dom/div #js {:className "stat-col"}
          (dom/div #js {:className "type-crash-total"}
              (om/build stat-link
                {:total (:crash-total data) :label (:crash-label data) :type (:crash-type data)}
                {:init-state state}))
          (dom/div #js {:className "type-total"}
            (om/build stat-link
              {:total (:type-total data) :label (:type-label data) :type (:type-type data)}
              {:init-state state}))
          (apply dom/ul #js {:className "break-down"}
            (om/build-all stat-break-down-view
              (:break-down data)
              {:init-state state}))))))

(defn text-stats-view
  [data owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (apply dom/div #js {:className "text-stats"}
        (dom/div #js {:className "total-crashes"}
          (om/build stat-link
            {:total (get-in data [:crashes :total-crashes]) :label " Total crashes" :type [:crashes :total-crashes]}
            {:init-state state}))
        (om/build-all stat-col-view
            (list (build-stat :injuries data) (build-stat :deaths data))
            {:init-state state})))))
