(ns ta-crash.routes
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [ta-crash.core :as ta-crash]
    [cljs.core.async :refer [<!]]
    [ta-crash.requester :as requester]
    [secretary.core :as secretary :refer-macros [defroute]]))

(defn get-area-type []
  (if-let [area-type (.getItem (.-localStorage js/window) "area")]
    (keyword area-type)
    :city))

(defroute home "/" []
  (ta-crash/render-page :home))

(defroute page-path "/:page-type/:identifier" {:as params}
  (go
    (let [page-type (keyword (:page-type params))
          identifier (keyword (:identifier params))
          area-type (get-area-type)
          data (<! (requester/get-data page-type area-type identifier))]
      (ta-crash/set-state-data! page-type data)
      (ta-crash/render-page page-type data))))
