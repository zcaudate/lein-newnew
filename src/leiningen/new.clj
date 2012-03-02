(ns leiningen.new
  "Generate project scaffolding based on a template."
  (:import java.io.FileNotFoundException))

(defn resolve-template [name]
  (let [sym (symbol (str "leiningen.new." name))]
    (if (try (require sym)
             (catch FileNotFoundException _ true))
      (println "Could not find template" name "on the classpath.")
      (resolve (symbol (str sym "/" name))))))

;; A lein-newnew template is actually just a function that generates files and
;; directories. We have a bit of convention: we expect that each template is on
;; the classpath and is based in a .clj file at `leiningen/new/`. Making this
;; assumption, a user can simply give us the name of the template he wishes to
;; use and we can `require` it without searching the classpath for it or doing
;; other time consuming things.
;;
;; Since our templates are just function calls just like Leiningen tasks, we can
;; also expect that a template generation function also be named the same as the
;; last segment of its namespace. This is what we call to generate the project.
;; If the template's namespace is not on the classpath, we can just catch the
;; FileNotFoundException and print a nice safe message.

(defn new*
  ([project project-name]
     (new* project "default" project-name))
  ([project template name & args]
   (if (and (re-find #"(?i)(?<!(clo|compo))jure" name)
            (not (System/getenv "LEIN_IRONIC_JURE")))
     (println "Sorry, names based on non-ironic *jure puns are not allowed.\n"
              "If you intend to use this name ironically, please set the\n"
              "LEIN_IRONIC_JURE environment variable and try again.")
     (when-let [f (resolve-template template)]
       (apply f name args)))))

(defn ^{:no-project-needed true
        :help-arglists '([project project-name]
                         [project template project-name & args])}
  new
  "Generate scaffolding for a new project based on a template.

If only one argument is passed, the default template is used and the
argument is treated as if it were the name of the project."
  [& args]
  (let [project (first args)]
    (if (or (map? project) (nil? project))
      (apply new* args)
      (apply new* nil args))))
