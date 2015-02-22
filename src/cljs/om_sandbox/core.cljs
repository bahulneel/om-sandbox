(ns om-sandbox.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(defonce app-state (atom {:screen {}
                          :device {}
                          :camera {:center [0 0]
                                   :scale 10}
                          :text "Hello Chestnut!"}))

(defn resize
  [app-state]
  (let [width (-> js/window
                  .-innerWidth
                  (max 240)
                  (- 5))
        height (-> js/window
                   .-innerHeight
                   (max 240)
                   (- 5))]
    (assoc app-state :screen {:width width
                              :height height})))

(defn device
  [app-state]
  (let [pixel-ratio (.-devicePixelRatio js/window)]
    (assoc app-state :device {:pixel-ration pixel-ratio})))

(defn init
  []
  (let [body (.. js/document
                 (getElementsByTagName "body")
                 (item 0))]
    (set! (.-onresize body) (fn []
                              (swap! app-state resize))))
  (swap! app-state #(-> %
                        device
                        resize)))

(init)

(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IRender
        (render [_]
          (let [screen (:screen app)]
            (html
             [:svg screen
              [:rect screen]])))))
    app-state
    {:target (. js/document (getElementById "app"))}))
