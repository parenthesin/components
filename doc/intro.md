# Introduction to com.github.parenthesin/components

Hey there, thanks a lot for your interest in learning more about the parenthesis/components library. First of all, we have to talk about "what exactly is a component and why I need it?"

## Component
Component is defined as a *tiny Clojure framework for managing the lifecycle and dependencies of software components with runtime state*. This means the Component can be seen as a dependency injection using immutable data structures. To give more context about it, you can watch Sierra's talk [Components Just Enough Structure](https://youtu.be/13cmHf_kt-Q?si=VribqpkKYOofgAWz).

To handle this entire explanation, imagine that you have a database connection to get. If you open so many connections and don't close them, your database will *break* as you increase the opened connections. By default, in object-oriented programming languages we can manage this as an object in memory - only one - that you pass as your parameters by your methods and use that connection - that's where the component came from: you have instanced objects in memory and you have to pass all of them by parameters to access what that object gives you (in our case of a database example, you can make any database operation with it). 

But the Component *magic* is more than that: is how you infer your dependency injection. To understand how it works, see the example below:
```clojure
(defn- build-system-map []
  (component/system-map
   :config (config/new-config)
   :http (http/new-http)
   :router (router/new-router routes/routes)
   :database (component/using (database/new-database) [:config])
   :webserver (component/using (webserver/new-webserver) [:config :http :router :database])))
```
> The above example is from [microservice-boilerplate](https://github.com/parenthesin/microservice-boilerplate).

You have some components that are configured: you have a `:config`, `:http`, `:router`, `:database` and `:webserver`. Look at the `:config` component: it only has itself, so this represents that `:config` doesn't have any dependency by default from another component. In another hand, when we look at `:database` we have `(component/using (database/new-database) [:config])`, but what that means? Simple: we have a component that starts with `(database/new-database)` but it depends of another component: `:config`! This means that `:database` will only start when `:config` is properly started! And now look to `:webserver`... It will start only after the initialization of `:config`, `:http`, `:router` and `:database`!

By default, the Component library implements a protocol that you have to refer to when you're building your components (a protocol is very similar to an interface from object-oriented programming) - the protocol name is Lifecycle, and it needs to have two main functions: a start and a stop function. Look at the example below:
```clojure
(ns com.example.your-application
  (:require [com.stuartsierra.component :as component]))

(defrecord Database [host port connection]
  ;; Implement the Lifecycle protocol
  component/Lifecycle

  (start [component]
    (println ";; Starting database")
    ;; In the 'start' method, initialize this component
    ;; and start it running. For example, connect to a
    ;; database, create thread pools, or initialize shared
    ;; state.
    (let [conn (connect-to-database host port)]
      ;; Return an updated version of the component with
      ;; the run-time state assoc'd in.
      (assoc component :connection conn)))

  (stop [component]
    (println ";; Stopping database")
    ;; In the 'stop' method, shut down the running
    ;; component and release any external resources it has
    ;; acquired.
    (.close connection)
    ;; Return the component, optionally modified. Remember that if you
    ;; dissoc one of a record's base fields, you get a plain map.
    (assoc component :connection nil)))
```
> This example came from the Component library README

Every time that you want to implement a new component, you will be implementing this protocol. If you want to access your component, you can define a function to start it and update its value, like this example:
```clojure
(defn new-database [host port]
  (map->Database {:host host :port port}))
```

Well, by default Component uses records instead of classical maps and that's why you use a function named `map->YourComponent` to access a map of properties of your component. You can see more about Records by reaching [defrecord](https://docs.clj.codes/org.clojure/clojure/clojure.core/defrecord/0) and see implementation examples.

Suppose you don't like the primary idea of using records. In that case, you can check out some Component alternatives, like [Integrant](https://github.com/weavejester/integrant) and [mount](https://github.com/tolitius/mount), and compare them with Component to see the general differences.

## Mocking components
Sometimes, creating a mock can be helpful with integration tests, so by default, this approach of using records and implementing some protocols can be useful for such cases. Let's dive deep into an example of our HTTP component.

Inside our components, we have an [HTTP component](https://github.com/parenthesin/components/blob/main/src/parenthesin/components/http/clj_http.clj) using [clj-http](https://github.com/dakrone/clj-http). To manage our tests, it's helpful to configure both an HTTP component to make real requests to the external world and an internal component to mock these requests and make our tests faster and more reliable.

First of all, we've to define a simple protocol for an HTTP provider that manages a request, like this one:
```clojure
(defprotocol HttpProvider
  (request
    [self request-input]))
```
And now, we can implement it for both cases! Look at the example below:
```clojure
(defrecord Http [_]
  component/Lifecycle
  (start [this] this)
  (stop  [this] this)

  HttpProvider
  (request
    [_self {:keys [method url] :as request-input}]
    (logs/log :info :http-out-message :method method :url url)
    (let [start-time (System/currentTimeMillis)
          {:keys [status] :as response} (request-fn request-input)
          end-time (System/currentTimeMillis)
          total-time (- end-time start-time)]
      (logs/log :info :http-out-message-response :response-time-millis total-time
                :status status)
      response)))

(defn new-http [] (map->Http {}))
```
Then, we have a component for HTTP that can handle requests and log them. But now imagine that we have to implement some similar component to handle our mock, but also implementing the `HttpProvider` protocol... How can we do it?
```clojure
(defrecord HttpMock [responses requests]
  component/Lifecycle
  (start [this] this)
  (stop  [this] this)

  HttpProvider
  (request
    [_self {:keys [url] :as req}]
    (swap! requests merge
           (assoc req :instant (System/currentTimeMillis)))
    (get-in @responses
            [url]
            {:status 500
             :body "Response not set in mocks!"})))

(defn new-http-mock
  [mocked-responses]
  (map->HttpMock {:responses (atom mocked-responses)
                  :requests (atom [])}))
```
*Voilà*! Now we have both components for HTTP implementing the same protocol and handling our interactions depending on our usage. By default, you can see this example running at the integration tests from [microservice-boilerplate](https://github.com/parenthesin/microservice-boilerplate/blob/main/test/integration/microservice_boilerplate/wallet_test.clj#L25).

## How to use it?
Well, now you have a full introduction to the *Component* structure. Now, we can deep dive into each component and understand its usability based on some examples! Check out:
- [Components](components.md)
- [Helpers](helpers.md)
