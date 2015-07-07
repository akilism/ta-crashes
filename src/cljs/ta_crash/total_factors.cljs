(ns ta-crash.total-factors
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ta-crash.aggregation :as agg]))


(defn factor-item
  [[type val :as item] owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "factor-item"}
        (dom/p #js {:className "factor-item-name"} (name type))
        (dom/p #js {:className "factor-item-val"} val)))))

(defn total-factors
  [factors owner]
  (reify
    om/IRender
    (render [this]
      (let [type (:type factors)
            items (dissoc factors :type)]
        (apply dom/div #js {:className "factor"}
          (om/build-all factor-item items))))))
