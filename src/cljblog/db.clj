(ns cljblog.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer [$set]]
            [crypto.password.pbkdf2 :as crypto])
  (:import
   [org.bson.types ObjectId]))

(def article-collection "articles")
(def users-collection "users")

(defn encrypy-password [password]
  (crypto/encrypt password))

(def db-connection-uri (or (System/getenv "CLJBLOG_MONGO_URI")
                           "mongodb://127.0.0.1/cljblog-test"))

(def db (-> db-connection-uri
            mg/connect-via-uri
            :db))

(defn create-article [user-email title body]
  (mc/insert db article-collection
             {:title title
              :body body
              :author user-email
              :created (new java.util.Date)
              :updated (new java.util.Date)}))
(defn list-articles []
  (mc/find-maps db article-collection))

(defn detail-article [id]
  (mc/find-map-by-id db article-collection (ObjectId. id)))

(defn delete-article [id]
  (mc/remove-by-id db article-collection (ObjectId. id)))

(defn update-article [user-email id title body]
  (mc/update-by-id db article-collection (ObjectId. id)
                   {$set
                    {:title title
                     :body body
                     :author user-email
                     :updated (new java.util.Date)}}))

(defn find-user-by-email [email]
  (mc/find-one db users-collection
               {:email email}))

(defn create-user [email password]
  (mc/insert db users-collection
             {:email email
              :password (encrypy-password password)
              :created (new java.util.Date)
              :updated (new java.util.Date)}))

(defn register [email password]
  (if (find-user-by-email email) nil (create-user email password)))

(defn login [email password]
  (let [user (find-user-by-email email)]
    (if (crypto/check password (get user "password")) true false)))