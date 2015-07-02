(ns ta-crash.server
  (:require [clojure.java.io :as io]
            [ta-crash.dev :refer [is-dev? inject-devmode-html browser-repl start-figwheel]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [net.cgrand.enlive-html :refer [deftemplate]]
            [net.cgrand.reload :refer [auto-reload]]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :as ring-json]
            [ring.util.response :as rr]
            [ta-crash.handlers :as handlers])
  (:gen-class))

(deftemplate page (io/resource "index.html") []
  [:body] (if is-dev? inject-devmode-html identity))

(defroutes routes
  (resources "/")
  (resources "/react" {:root "react"})
  (context "/api/:type/:identifier" [type identifier :as req]
    (GET "/" [] (rr/response (handlers/get-crash-data type identifier req)))
    (GET "/:date-range/" [date-range] (rr/response (handlers/get-crash-data type identifier date-range req)))
    (GET "/:date-range/:date-aggregate" [date-range date-aggregate] (rr/response (handlers/get-crash-data  type identifier date-range date-aggregate req))))
  (GET "/*" req (page)))

(def http-handler
  (if is-dev?
    (-> #'routes
        (reload/wrap-reload)
        (ring-json/wrap-json-response)
        (wrap-defaults (assoc site-defaults :proxy true)))
    ; (reload/wrap-reload (wrap-defaults #'routes api-defaults))
    (-> routes
      (ring-json/wrap-json-response)
      (wrap-defaults (assoc site-defaults :proxy true)))))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (println (format "Starting web server on port %d." port))
    (run-jetty http-handler {:port port :join? false})))

(defn run-auto-reload [& [port]]
  (auto-reload *ns*)
  (start-figwheel))

(defn run [& [port]]
  (when is-dev?
    (run-auto-reload))
  (run-web-server port))

(defn -main [& [port]]
  (run port))
