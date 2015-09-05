(ns ta-crash.header
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [secretary.core :as secretary]))

(defn header-display
  [[type identifier]]
  (case type
    :precinct (str (name identifier) " Precinct")
    :city-council (str "City Council District " (name identifier))
    :community-board (str "Community Board " (name identifier))
    (name identifier)))

(defn sub-nav-display
  [type]
  (case type
    :city-council "city council district"
    :community-board "community board"
    :neighborhood "neighborhood"
    :precinct "police precinct"
    :zipcode "zip code"
    (name type)))

(defn active-classer
  [active-type type]
  (if (= type active-type) "active" "inactive"))

(defn handle-nav-button
  [e page-type [type identifier]]
  (.preventDefault e)
  (let [page (.-value (.-target e))]
    (println (keyword page) " - " page-type)
    (cond
      (= (keyword page) page-type) nil
      :else (set! (.-location js/window) (str "/" page "/" (name type) "/" (name identifier))))))

(defn handle-sub-nav
  [e type nav-chan]
  (.preventDefault e)
  (put! nav-chan type))

(defn sub-nav-view
  [data owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (dom/li #js {:className (active-classer (:type state) data)}
        (dom/a #js {:href "#" :onClick (fn [e] (handle-sub-nav e data (:nav-chan state)))}
          (sub-nav-display data))))))

(defn header-view
  [{:keys [areas, type-ident, page-type] :as data} owner]
  (reify
    om/IInitState
    (init-state [_]
    data)
    om/IRenderState
    (render-state [_ state]
      (dom/div #js {:className "header"}
        (dom/div #js {:className "header-left"}
          (dom/div #js {:className "site-name"} "CrashPad")
          (dom/h1 #js {:className "identifier"} (header-display type-ident)))
        (dom/div #js {:className "sub-nav"}
          (apply dom/ul nil
            (om/build-all sub-nav-view areas
              {:init-state {:type (first type-ident) :nav-chan (:nav-chan state)}})))))))
