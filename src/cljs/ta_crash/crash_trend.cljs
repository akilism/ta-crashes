(ns ta-crash.crash-trend
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [ta-crash.aggregation :as agg]
            [ta-crash.header :as header]
            [ta-crash.text-stats :as text-stats]
            [ta-crash.line-graph :as line-graph]))

(defn get-totals
  [[t & ts] data acc]
  (cond
    (nil? t) acc
    :else (recur ts data (assoc acc t (first (filter #(= t (:type %)) data))))))

(defn get-type-identifier
  [data]
  (let [type (first (:type data))
        identifier (first (:identifier data))]
    [type identifier]))

(defn get-line-chart-data
  [path data]
  (let [[type identifier] (get-type-identifier data)]
    ;(println type " - " identifier)
    (line-graph/build-data path (get-in data [:data type identifier]))))

(defn crash-trend-view
  [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [set-line-chart-data (om/get-state owner :set-line-chart-data)]
        (go (loop []
          (let [chart-data-path (<! set-line-chart-data)]
            (om/transact! data :line-chart-dimension (fn [_] chart-data-path))
            (recur))))))
    om/IInitState
    (init-state [_]
      (let [[type identifier] (get-type-identifier data)
            trend-data (first (agg/total-data agg/sum-all-data (get-in data [:data type identifier])))]
        (assoc data :trend-data trend-data :set-line-chart-data (chan))))
    om/IRenderState
    (render-state
      [_ state]
      (dom/div #js {:className "container"}
        (om/build header/header-view ())
        (om/build text-stats/text-stats-view
          (get-totals [:crashes :injured :killed] (get-in state [:trend-data :totals]) {})
          {:init-state {:set-line-chart-data (:set-line-chart-data state)}})
        (om/build line-graph/line-graph-view (get-line-chart-data (:line-chart-dimension data) data))))))
