(ns cljblog.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as response]
            [ring.middleware.session :as session]
            [cljblog.db :as db]
            [cljblog.pages :as p]))

(defroutes app-routes
  (GET "/" [] (p/index (db/list-articles)))
  (GET "/test" [] (println "Hello"))
  (GET "/article/:id" [id] (p/article (db/detail-article id)))
  (GET "/register" [] (p/register-user))
  (POST "/register" [email password]
    (do (db/register email password)
        (response/redirect "/")))
  (GET "/login" [:as {session :session}] (if (:login session) (response/redirect "/") (p/login)))
  (POST "/login" [email password]
    (if (db/login email password)
      (-> (response/redirect "/")
          (assoc-in [:session :login] true)
          (assoc-in [:session :user-email] email))
      (response/redirect "/login")))
  (GET "/logout" []
    (-> (response/redirect "/login")
        (assoc-in [:session :login] false)
        (assoc-in [:session :user-email] "")))
  (route/not-found "Not Found"))

(defroutes private-routes
  (GET "/article/new" [] (p/edit-article nil))
  (POST "/article" [:as {session :session} title body]
    (do (db/create-article (:user-email session) title body)
        (response/redirect "/")))
  (GET "/article/:id/edit" [id] (p/edit-article (db/detail-article id)))
  (POST "/article/:id" [:as {session :session} id title body]
    (do (db/update-article (:user-email session) id title body)
        (response/redirect (str "/article/" id))))
  (POST "/article/:id/delete" [id]
    (do (db/delete-article id)
        (response/redirect "/"))))

(defn login-middleware [handler]
  (fn [request] (if (-> request :session :login)
                  (handler request)
                  (response/redirect "/login"))))

(def app
  (-> (routes (wrap-routes private-routes login-middleware)
              app-routes)
      (wrap-defaults site-defaults)
      (session/wrap-session)))