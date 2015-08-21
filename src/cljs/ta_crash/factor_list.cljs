(ns ta-crash.factor-list
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

;; needs a channel to send factor selections down. (allow multi-select by toggle)
(defn factor-view
  [data owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (dom/li #js {:className "factor"}
        (dom/a nil
          (dom/span #js {:className "name"} (:name data))
          (dom/span #js {:className "count"} (:count data)))))))

;;needs a data object that is a collection of factors {:name "" :count 0}
(defn factor-list-view
  [data owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (apply dom/ul #js {:className "factor-list"}
        (om/build-all factor-view (:factors state) {:init-state state})))))
