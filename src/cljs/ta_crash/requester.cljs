(ns ta-crash.requester
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!] :as async]))

(defn build-api-url
  ([]
    "/api/p/1")
  ([page-type area-type  identifier]
    (str "/api/" (name page-type) "/" (name area-type) "/" (name identifier))))

(defn make-request
  [url]
  (println "Requesting: " url)
  (go (let [response (<! (http/get url))
            body (:body response)]
        (println "Response status: " (:status response))
        body)))

(defn get-data
  ([]
   (go (let [url (build-api-url)
            data (<! (make-request url))]
        data)))
  ([page-type area-type identifier]
   (go (let [url (build-api-url page-type area-type  identifier)
             data (<! (make-request url))]
         data))))
