
A luminus web app that executes HTTP requests asynchronously and stores the result in file that can be downloaded from web console.
generated using Luminus version "2.9.10.69"

FIXME

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

First, you need a database. If you're using PostgreSQL,
create a new database:

```bash
createdb test_db
```

Then run the migrations using dev-config.edn where you can put required values in edn file:

```bash
java -Dconf="dev-config.edn" -jar web-vendor-campaign.jar migrate
```

To start a web server for the application, run:

```bash
java -Dconf="dev-config.edn" -jar web-vendor-campaign.jar
```

And the edn file configuration:
```clojure
{;; database url
 :database-url <database url>
 
 :base_url <base url name>
 :search_url_path <search path url>
 :get_url_path <get path url>

 :username <username>
 :password <password>

 ;; output-file-path and dest-path have to be same
 :output-file-path <output file path>
 ;; output-file-path and dest-path have to be same
 :dest-path <destination file path>

 :download-tag "processed_"

 ;; Number of rows in the list page
 :row-limit 20}
```


####Author
#####Anik Chowdhury
## License