(ns ta-crash.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [<!]]
            [ta-crash.requester :as requester]
            [ta-crash.total-groups :as total-groups]))

(enable-console-print!)

(defonce app-state (atom {:data []
                          :active-years [2015]}))

(defn render-root []
  (go
    (let [data (<! (requester/get-data))]
      (swap! app-state assoc :data data)
      (om/root
        total-groups/total-groups-view
        app-state
        {:target (. js/document (getElementById "app"))})))
;  (om/root
;    (fn [app owner]
;      (reify
;        om/IRender
;        (render [_]
;          (dom/h1 nil (:text app)))))
;    app-state
;    {:target (. js/document (getElementById "app"))})
)

(defn main []
  (render-root))
