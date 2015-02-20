(ns om-sandbox.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(defonce pixel-ratio (.-devicePixelRatio js/window))

(defonce app-state (atom {:screen {}
                          :camera {:center [0 0]
                                   :scale 10}
                          :text "Hello Chestnut!"}))

(defn resize
  []
  (let [width (.-innerWidth js/window)
        height (.-innerHeight js/window)]
    (swap! app-state assoc :screen {:width width
                                    :height height})))

(defn p->u
  [{:keys [scale]} p]
  (/ p scale))

(defn update-view
  [screen camera]
  (let [{:keys [width height]} screen
        {:keys [center scale]} camera
        [x y] center
        width (/ width pixel-ratio)
        height (/ height pixel-ratio)
        view-width (/ width scale)
        view-height (/ height scale)
        view-x (- x (/ view-width 2))
        view-y (- y (/ view-height 2))
        view (apply str (interpose " " [view-x view-y view-width view-height]))]
    (assoc screen :viewBox view)))

(defn grid-square
  [camera x y size]
  (let [{:keys [scale]} camera
        s {:fill :none :stroke :grey :stroke-width (p->u camera 1)}
        sl (assoc s :stroke-dasharray "0.01, 0.01")
        xm (+ x size)
        ym (+ y size)
        lines-x (for [x (range x xm)]
                  [:line (merge sl {:x1 x :y1 y :x2 x :y2 ym})])
        lines-y (for [y (range y ym)]
                  [:line (merge sl {:x1 x :y1 y :x2 xm :y2 y})])
        lines (concat lines-x lines-y)]
    (apply vector
           :g
           [:rect (merge s {:x x :y y :width size :height size})]
           lines)))

(defn grid
  [camera]
  (let [{:keys [scale]} camera
        n (* 3 scale)]
    [:g
     (for [x (range (- n) n scale)
           y (range (- n) n scale)]
       (grid-square camera x y scale))]))

(resize)

(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IRender
        (render [_]
          (let [screen (:screen app)
                camera (:camera app)
                view (update-view screen camera)
                g (grid camera)]
            (html
             [:svg view g])))))
    app-state
    {:target (. js/document (getElementById "app"))}))
