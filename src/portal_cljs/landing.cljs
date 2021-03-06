(ns portal-cljs.landing
  (:require [portal-cljs.components :refer [Tab TabContent]]
            [portal-cljs.datastore :as datastore]
            [portal-cljs.orders :refer [OrdersPanel]]
            [portal-cljs.state :refer [landing-state]]
            [portal-cljs.users :refer [UsersPanel]]
            [portal-cljs.utils :refer [base-url]]
            [portal-cljs.vehicles :refer [VehiclesPanel]]
            [reagent.core :as r]))

(def tab-content-toggle (r/cursor landing-state [:tab-content-toggle]))

(defn top-navbar-comp
  "A navbar for the top of the application
  props is:
  {:nav-bar-collapse ; r/atom boolean
  }"
  []
  (let [nav-bar-collapse (r/cursor landing-state [:nav-bar-collapse])]
    (fn []
      [:div
       [:div {:class "navbar-header"}
        [:button {:type "button"
                  :class "navbar-toggle"
                  :on-click #(do (.preventDefault %)
                                 (swap! nav-bar-collapse not))
                  :data-toggle "collapse"
                  :data-target ".navbar-collapse"}
         [:span {:class "sr-only"} "Toggle Navigation"]
         [:span {:class "icon-bar"}]
         [:span {:class "icon-bar"}]
         [:span {:class "icon-bar"}]]
        [:a {:class "navbar-brand" :href "#"}
         [:img {:src (str base-url "/images/logo.png")
                :alt "PURPLE"
                :class "purple-logo"}]]]
       [:ul {:class "nav navbar-right top-nav hidden-xs hidden-sm"}
        [:li {:style {:padding "15px"}}
         [:span {:class "grey-color"}
          @(r/cursor landing-state [:user-email])]
         [:span {:class "orange-color"} " | "]
         [:a {:href (str base-url "logout")
              :style {:display "inline"
                      :padding "0px"
                      :padding-right "15px"}} "LOG OUT"]]]])))

(defn side-navbar-comp
  "Props contains:
  {
  :tab-content-toggle ; reagent atom, toggles the visibility of tab-content
  :toggle             ; reagent atom, toggles if tab is active
  :toggle-key         ; keyword, the keyword associated with tab in :toggle
  }
  "
  [props]
  (let [nav-bar-collapse (r/cursor landing-state [:nav-bar-collapse])
        on-click-tab (fn []
                       (.scrollTo js/window 0 0)
                       (reset! nav-bar-collapse true))]
    (fn []
      [:div {:class (str "collapse navbar-collapse sidebar-nav "
                         (when-not @nav-bar-collapse
                           "in"))}
       ;; navbar-ex1-collapse
       [:ul {:class "nav navbar-nav side-nav side-nav-color"}
        [:li {:class "hidden-lg hidden-md"}
         [:a {:href (str base-url "logout")} "LOG OUT"]]
        [Tab {:default? false
              :toggle-key :vehicles-view
              :toggle (:tab-content-toggle props)
              :on-click-tab on-click-tab}
         [:div "VEHICLES"]]
        [Tab {:default? true
              :toggle-key :orders-view
              :toggle (:tab-content-toggle props)
              :on-click-tab on-click-tab}
         [:div "ORDERS"]]
        (when (datastore/account-manager?)
          [Tab {:default? false
                :toggle-key :users-view
                :toggle (:tab-content-toggle props)
                :on-click-tab on-click-tab}
           [:div "USERS"]])
        ]])))

;; based on https://github.com/IronSummitMedia/startbootstrap-sb-admin
(defn app
  []
  (let []
    (fn []
      [:div {:id "wrapper"}
       [:nav {:class (str "navbar navbar-default navbar-fixed-top "
                          "navbar-inverse nav-bar-color")
              :role "navigation"}
        [top-navbar-comp {}]
        [side-navbar-comp {:tab-content-toggle tab-content-toggle}]]
       [:div {:id "page-wrapper"
              :class "page-wrapper-color"}
        [:div {:class "container-fluid tab-content"}
         ;; users-view
         (when (datastore/account-manager?)
           [TabContent
            {:toggle (r/cursor tab-content-toggle [:users-view])
             :id "users" }
            [UsersPanel @datastore/users]])
         ;; vehicles page
         [TabContent
          {:toggle (r/cursor tab-content-toggle [:vehicles-view])
           :id "vehicles"}
          [VehiclesPanel @datastore/vehicles]]
         ;; orders
         [TabContent
          {:toggle (r/cursor tab-content-toggle [:orders-view])
           :id "orders"}
          [OrdersPanel @datastore/orders]]]]])))

(defn init-landing
  []
  (r/render-component [app] (.getElementById js/document "app")))
