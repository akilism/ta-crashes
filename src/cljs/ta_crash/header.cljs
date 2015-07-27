(ns ta-crash.header
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [secretary.core :as secretary]))

(defmulti header-display (fn [[type _]] type))

(defmethod header-display :precinct
  [[_ identifier]]
  (str (name identifier) " Precinct"))

(defmethod header-display :city-council
  [[_ identifier]]
  (str "City Council District " (name identifier)))

(defmethod header-display :community-board
  [[_ identifier]]
  (str "Community Board " (name identifier)))

(defmethod header-display :default
  [[_ identifier]]
  (name identifier))

(defmulti sub-nav-display (fn [type] type))

(defmethod sub-nav-display :precinct
  [_]
  "police precinct")

(defmethod sub-nav-display :city-council
  [_]
  "city council district")

(defmethod sub-nav-display :community-board
  [_]
  "community board")

(defmethod sub-nav-display :zip-code
  [_]
  "zip code")

(defmethod sub-nav-display :default
  [type]
  (name type))

(defn active-classer
  [active-type type]
  (if (= type active-type) "active" "inactive"))

(defn sub-nav-view
  [data owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (dom/li #js {:className (active-classer (:type state) data)} (sub-nav-display data)))))

(defn handle-nav-button
  [e page-type [type identifier]]
  (.preventDefault e)
  (let [page (.-value (.-target e))]
    (println (keyword page) " - " page-type)
    (cond
      (= (keyword page) page-type) nil
      :else (set! (.-location js/window) (str "/" page "/" (name type) "/" (name identifier))))))

(defn header-view
  [{:keys [areas, type-ident, page-type]} owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "header"}
        (dom/div #js {:className "header-left"}
          (dom/div #js {:className "site-name"} "CrashPad")
          (dom/h1 #js {:className "identifier"} (header-display type-ident)))
        (dom/div #js {:className "header-right"}
          (dom/button #js {:onClick (fn [e] (handle-nav-button e (first page-type) type-ident)) :value "crash-map" :className (active-classer (first page-type) :crash-map)} "CrashMap")
          (dom/button #js {:onClick (fn [e] (handle-nav-button e (first page-type) type-ident)) :value "crash-rank" :className (active-classer (first page-type) :crash-rank)} "CrashRank")
          (dom/button #js {:onClick (fn [e] (handle-nav-button e (first page-type) type-ident)) :value "crash-trend" :className (active-classer (first page-type) :crash-trend)} "CrashTrend"))
        (dom/div #js {:className "sub-nav"}
          (apply dom/ul nil
            (om/build-all sub-nav-view areas
              {:init-state {:type (first type-ident)}})))))))
