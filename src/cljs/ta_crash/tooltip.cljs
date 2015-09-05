(ns ta-crash.tooltip
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(def tool-tip-offset {:x 5 :y -10})
(defn get-display
  [show]
  (if show
    "block"
    "none"))
(defn to-px
  [val]
  (str val "px"))
(defn tool-tip
  [data owner]
  (reify
    om/IInitState
    (init-state [_]
      data)
    om/IRenderState
    (render-state
      [_ state]
      (let [display (get-display (:show data))
            left (-> data :pos :x (- (:x tool-tip-offset)) to-px)
            top (-> data :pos :y (- (:y tool-tip-offset)) to-px)
            display-f (:display-f data)]
        (dom/div #js {:className "tool-tip" :style #js {:left left :top top :display display}}
          (when (:identifier data)
            (display-f (:type data) (:identifier data))))))))
