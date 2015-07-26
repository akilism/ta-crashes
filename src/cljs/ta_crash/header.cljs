(ns ta-crash.header
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]))

(defn header-view
  [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "header"} "This is the header"))))
