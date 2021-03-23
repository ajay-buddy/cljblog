(ns cljblog.pages
  (:require
   [hiccup.page :refer [html5]]
   [hiccup.form :as form]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn base-page [& body]
  (html5
   [:head [:title "Clojure Blog"]
    [:link {:rel "stylesheet"
            :href "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css"
            :integrity "sha384-JcKb8q3iqJ61gNV9KGb8thSsNjpSL0n8PARn9HuZOnIxN0hoP+VmmDGMN5t9UJ0Z"
            :crossorigin "anonymous"}]]
   [:body
    [:div.container
     [:nav.navbar.navbar-expand-lg.navbar-light.bd-light
      [:a.navbar-brand {:href "/"} "Clojure Blog"]
      [:div.navbar-nav.ml-auto
       [:a.nav-item.nav-link {:href "/article/new"} "New Article"]
       [:a.nav-item.nav-link {:href "/login"} "Login"]
       [:a.nav-item.nav-link {:href "/register"} "Register"]
       [:a.nav-item.nav-link {:href "/logout"} "Logout"]]
      [:hr]]
     body]]))

(def preview-len 250)
(defn- cut-body [body]
  (if (> (.length body) preview-len)
    (subs body 0 preview-len)
    body))

(defn index [articles]
  (base-page
   (for [a articles]
     [:div
      [:h2 [:a {:href (str "/article/" (:_id a))} (:title a)]]
      [:p (-> a :body cut-body)]])))

(defn article [a]
  (base-page
   [:small (str "Created:  " (:created a))]
   [:br]
   [:small (str "Updated:  " (:updated a))]
   [:h1 (:title a)]
   [:small (str "Author:  " (or (:author a) "Anonymous"))]
   [:p (:body a)]
   [:div {:class "btn-group"}
    (form/form-to
     [:get (str "/article/" (:_id a) "/edit")]
     (anti-forgery-field)
     (form/submit-button {:class "btn btn-primary"} "Edit!"))
    (form/form-to
     [:post (str "/article/" (:_id a) "/delete")]
     (anti-forgery-field)
     (form/submit-button {:class "btn btn-primary"} "Delete!"))]))

(defn edit-article [article]
  (base-page
   (form/form-to
    [:post (if article
             (str "/article/" (:_id article))
             "/article")]
    [:div.form-group
     (form/label "title" "Title")
     (form/text-field {:class "form-control"} "title" (:title article))]
    [:div.form-group
     (form/label "body" "Body")
     (form/text-area {:class "form-control"} "body" (:body article))]

    (anti-forgery-field)
    (form/submit-button {:class "btn btn-primary"} "Save!"))))

(defn register-user []
  (base-page
   (form/form-to
    [:post "register"]
    [:div.form-group
     (form/label "email" "Email")
     (form/text-field {:class "form-control"} "email")]
    [:div.form-group
     (form/label "password" "Password")
     (form/password-field {:class "form-control"} "password")]

    (anti-forgery-field)
    (form/submit-button {:class "btn btn-primary"} "Register"))))

(defn login [& [msg]]
  (base-page
   (when msg
     [:div.alert.alert-danger msg])
   (form/form-to
    [:post "login"]
    [:div.form-group
     (form/label "email" "Email")
     (form/text-field {:class "form-control"} "email")]

    [:div.form-group
     (form/label "password" "Password")
     (form/password-field {:class "form-control"} "password")]

    (anti-forgery-field)
    (form/submit-button {:class "btn btn-primary"} "Login"))))