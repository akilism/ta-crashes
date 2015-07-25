(ns ta-crash.routes
  (:require
    [ta-crash.core :as ta-crash]
    [secretary.core :as secretary :refer-macros [defroute]]))

;(defmulti load-page
;  (fn [type &_] (keyword type)))

;(defmethod load-page :crash-map
;  [_ identifier date-start date-end date-display]
;  )

(defroute home-page "/" []
  (ta-crash/render-page :home))

(defroute page-path "/:page-type/:identifier" {:as params}
  (println (str (:page-type params))))

