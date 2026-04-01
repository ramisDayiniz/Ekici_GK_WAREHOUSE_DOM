# MidEng GK8.2 Document Oriented Middleware using MongoDB

##### Verfasser: Ramis Ekici

##### Datum: 01.04.2026

# Einführung

Diese Aufgabe markiert den Wechsel von relationalen Datenbanken (SQL) hin zu einer flexiblen, dokumentenbasierten Speicherung (NoSQL) mit **MongoDB**. Für das Backend wird wieder Java **SpringBoot** verwendet. 



# Vorbereitung

Zuerst muss das Repo von dem Professor geklont bzw. in ein anderen Repo kopiert werden. Dafür das Repo vom Professor klonen und dann:

```git
git remote remove origin

git remote add origin
https://github.com/ramisDayiniz/Ekici_DEZSYS_GK81_WAREHOUSE_ORM.git

git push -f --set-upstream origin main
```

Ein Docker **MongoDB**:

```docker
docker pull mongo

docker run -d -p 27017:27017 --name mongo-warehouse mongo
```



# Fragestellungen

Hier sind die Antworten für dein Protokoll, kurz und prägnant formuliert:

### 4 Vorteile von NoSQL gegenüber RDBMS

1. **Flexibilität:** Kein festes Schema; Felder können jederzeit hinzugefügt werden.

2. **Skalierbarkeit:** Einfache horizontale Skalierung (Sharding) über viele Server.

3. **Performance:** Hohe Schreibgeschwindigkeiten und schnelle Abfragen bei hierarchischen Daten.

4. **Natürliches Datenformat:** Speicherung direkt als JSON/Dokument (passend zu modernen Programmiersprachen).

### 4 Nachteile von NoSQL gegenüber RDBMS

1. **Keine komplexen Joins:** Verknüpfungen zwischen Tabellen sind schwierig oder nicht vorhanden.

2. **Geringere Konsistenz:** Oft nur "Eventual Consistency" statt strikter ACID-Transaktionen.

3. **Datenredundanz:** Informationen werden oft mehrfach gespeichert (Denormalisierung).

4. **Fehlende Standardisierung:** Jede NoSQL-Datenbank hat eine eigene Abfragesprache (kein einheitliches SQL).

### Schwierigkeiten bei der Zusammenführung (Aggregation)

- **Keine Joins:** Daten müssen händisch im Applikationscode oder über komplexe Aggregation-Pipelines kombiniert werden.

- **Inkonsistenz:** Daten könnten an einer Stelle aktualisiert worden sein, an einer anderen (redundanten) Stelle aber noch veraltet sein.

### Arten von NoSQL-Datenbanken & Vertreter

| **Art**                  | **Vertreter**    |
| ------------------------ | ---------------- |
| **Dokumentenorientiert** | MongoDB          |
| **Key-Value Store**      | Redis            |
| **Spaltenorientiert**    | Apache Cassandra |
| **Graph-Datenbank**      | Neo4j            |

### CAP-Theorem Abkürzungen

Das CAP-Theorem besagt, dass ein verteiltes System nur 2 dieser 3 Eigenschaften gleichzeitig garantieren kann:

- **CA (Consistency & Availability):** Daten sind überall gleich und das System antwortet immer (schwer in verteilten Netzen).

- **CP (Consistency & Partition Tolerance):** Daten sind überall gleich und das System überlebt Netzwerkausfälle, ist dann aber evtl. kurzzeitig nicht verfügbar (Beispiel: **MongoDB**).

- **AP (Availability & Partition Tolerance):** Das System ist immer verfügbar und überlebt Ausfälle, zeigt aber eventuell kurzzeitig veraltete Daten an.



### MongoDB Shell Befehle (Abtestat)

**1. Lagerstand eines Produkts über ALLER Lager anzeigen:**

JavaScript

```
db.warehouses.aggregate([
  { $unwind: "$products" },
  { $match: { "products.productID": "P-1-10" } },
  { $project: { _id: 0, warehouseID: 1, name: 1, "products.productQuantity": 1 } }
])
```

**2. Lagerstand eines Produkts eines BESTIMMTEN Lagers anzeigen:**

JavaScript

```
db.warehouses.find(
  { "warehouseID": "WH-01", "products.productID": "P-1-10" },
  { _id: 0, "products.$": 1 }
)
```





# Praxis

## Model

Wie immer brauchen wir Model klassen für das Logik. Diese sind folgende:

#### Productdata

Diese Klasse sind Objekten für die Produkten selber.

```java
package warehouse.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.data.annotation.Id;

public class ProductData {

    @Id
	private String ID;

	private String warehouseID;
	private String productID;
	private String productName;
	private String productCategory;
	private double productQuantity;

	/**
	 * Constructor
	 */
	public ProductData() {
	}

	public ProductData(String warehouseID, String productID, String productName, String productCategory, double productQuantity ) {
		super();
		this.warehouseID = warehouseID;
		this.productID = productID;
		this.productName = productName;
		this.productCategory = productCategory;
		this.productQuantity = productQuantity;
	}
	/**
	 * Methods
	 */
	@Override
	public String toString() {
		String info = String.format("Product Info: WarehouseID = %s, ProductID = %s, ProductName = %s, ProductCategory = %s, ProductQuantity = %4.1f",
			warehouseID, productID, productName, productCategory, productQuantity );
		return info;
	}

    // Getter und Setter
}


```



#### Warehouse

Diese Klasse sind die Lager Objekten selber.

```java
@Document(collection = "warehouses")
public class Warehouse {

    @Id
    private String id;

    private String warehouseID;
    private String name;
    private String city;
    private List<ProductData> products = new ArrayList<>();

    public Warehouse() {}

    public Warehouse(String warehouseID, String name, String city) {
        this.warehouseID = warehouseID;
        this.name = name;
        this.city = city;
    }

    public void addProduct(ProductData product) {
        this.products.add(product);
    }

    // Getter und Setter
}
```





## Repository

Um Methoden zu erstellen von Model, Datenbank usw. Daten zu nehmen und diese zubearbeiten, brauchen wir eine RepositoryKlasse. Diesmal verwenden wir als Interface oder Klasse MongoRepository. Aber das Verwenden und die Benutzung der Repositories sind identisch.

```java
public interface WarehouseRepository extends MongoRepository<Warehouse, String> {

//    public ProductData findByProductID(String productID);


    // Findet ein komplettes Lager-Dokument anhand der Geschäfts-ID (z.B. "WH-01")
    Warehouse findByWarehouseID(String warehouseID);

    // Falls du Lager nach Stadt suchen willst (optional für Berichte)
//    List<Warehouse> findByCity(String city);
}
```



## Controller

Nach den Model Klassen brauchen wir natürlich für Rest Schnittstellen noch eine Controller Klasse, wo wir alle REST-Funktionen implementieren.

```java
@RestController
public class WarehouseController {

    @Autowired
    private WarehouseRepository repository;

    @PostMapping("/warehouse")
    public Warehouse addWarehouse(@RequestBody Warehouse warehouse) {
        return repository.save(warehouse);
    }

    @GetMapping("/warehouse")
    public List<Warehouse> getAllWarehouses() {
        return repository.findAll();
    }

    @GetMapping("/warehouse/{id}")
    public Warehouse getWarehouseById(@PathVariable String id) {
        return repository.findByWarehouseID(id);
    }

    @DeleteMapping("/warehouse/{id}")
    public void deleteWarehouse(@PathVariable String id) {
        Warehouse wh = repository.findByWarehouseID(id);
        if (wh != null) repository.delete(wh);
    }

    // --- PRODUCT ENDPOINTS ---

    // POST /product: Fügt ein Produkt zu einem Lager hinzu (Lager-ID muss im ProductData Objekt sein)
    @PostMapping("/product")
    public Warehouse addProductToWarehouse(@RequestBody ProductData product) {
        Warehouse wh = repository.findByWarehouseID(product.getWarehouseID());
        if (wh != null) {
            wh.addProduct(product);
            return repository.save(wh);
        }
        return null; // Oder Error Handling
    }

    // GET /product: Alle Produkte über alle Lager hinweg finden
    @GetMapping("/product")
    public List<ProductData> getAllProducts() {
        return repository.findAll().stream()
                .flatMap(wh -> wh.getProducts().stream())
                .collect(Collectors.toList());
    }

    // GET /product/{id}: Ein Produkt anhand seiner ID suchen (über alle Lager)
    @GetMapping("/product/{id}")
    public List<ProductData> getProductByIdAcrossWarehouses(@PathVariable String id) {
        return repository.findAll().stream()
                .flatMap(wh -> wh.getProducts().stream())
                .filter(p -> p.getProductID().equals(id))
                .collect(Collectors.toList());
    }

    // DELETE /product/{id}: Löscht ein Produkt mit ID {id} aus ALLEN Lagern
    @DeleteMapping("/product/{id}")
    public void deleteProductFromAllWarehouses(@PathVariable String id) {
        List<Warehouse> allWarehouses = repository.findAll();
        for (Warehouse wh : allWarehouses) {
            boolean removed = wh.getProducts().removeIf(p -> p.getProductID().equals(id));
            if (removed) {
                repository.save(wh);
            }
        }
    }
}
```





## Application

Schließlich brauchen wir natürlich eine klasse, wo wir SpringBoot ausführen können und natürlich die Testdaten reinspeichern können.

```java
@SpringBootApplication
public class Application implements CommandLineRunner {

	@Autowired
	private WarehouseRepository repository;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		repository.deleteAll(); // Datenbank aufräumen

		String[] cities = {"Linz", "Wien", "Graz", "Salzburg", "Innsbruck"};
		String[] categories = {"Getraenk", "Waschmittel", "Tierfutter", "Reinigung", "Elektronik", "Obst"};

		for (int i = 1; i <= 5; i++) {
			Warehouse wh = new Warehouse("WH-0" + i, "Lager " + cities[i-1], cities[i-1]);

			// 60 Produkte pro Lager = 300 insgesamt
			for (int p = 1; p <= 60; p++) {
				String pId = "P-" + i + "-" + p;
				String pName = categories[p % categories.length] + " Artikel " + p;
				double qty = Math.random() * 500; // Zufälliger Lagerbestand

				wh.addProduct(new ProductData(wh.getWarehouseID(), pId, pName, categories[p % categories.length], qty));
			}
			repository.save(wh);
		}

		System.out.println("Vertiefung: 5 Lager und 300 Produkte wurden erstellt!");
	}
}
```





# Ergebnisse

![](C:\Users\ekici\AppData\Roaming\marktext\images\2026-04-01-21-32-54-image.png)



### http://localhost:8080/warehouse

![](C:\Users\ekici\AppData\Roaming\marktext\images\2026-04-01-21-34-12-image.png)



### http://localhost:8080/product

![](C:\Users\ekici\AppData\Roaming\marktext\images\2026-04-01-21-35-19-image.png)



### http://localhost:8080/warehouse/WH-01

### ![](C:\Users\ekici\AppData\Roaming\marktext\images\2026-04-01-21-36-58-image.png)

### Unterschied der Endpunkte

- **`/warehouse` (Dokumenten-Sicht):** Zeigt das Lager als **Hauptobjekt**. Die Produkte sind darin "eingebettet" (verschachtelt). Das entspricht der echten Speicherung in MongoDB.

- **`/product` (Listen-Sicht):** Zeigt eine **flache Liste** aller Produkte. Deine Middleware (Java) hat die Verschachtelung aufgelöst, damit man alle Artikel direkt untereinander sehen kann.



# MongoBash

![](C:\Users\ekici\AppData\Roaming\marktext\images\2026-04-01-21-30-39-image.png)

### Teil 1: CRUD

Dokumentiere diese Befehle und mach jeweils einen Screenshot vom Ergebnis.

1. **Create (Ein Produkt zu einem Lager hinzufügen):**
   
   ```
   db.warehouses.updateOne(
    { "warehouseID": "WH-01" },
    { $push: { "products": { "productID": "P-999", "productName": "Shell-Test", "productCategory": "Test", "productQuantity": 100 } } }
   )
   
   ```

2. **Read (Ein bestimmtes Lager finden):**
   
   ```
   db.warehouses.find({ "warehouseID": "WH-01" }).pretty()
   ```

3. **Update (Die Menge eines Produkts ändern):**
   
   ```
   db.warehouses.updateOne(
    { "warehouseID": "WH-01", "products.productID": "P-999" },
    { $set: { "products.$.productQuantity": 150 } }
   )
   ```

4. **Delete (Ein Produkt aus einem Lager löschen):**
   
   ```
   db.warehouses.updateOne(
    { "warehouseID": "WH-01" },
    { $pull: { "products": { "productID": "P-999" } } }
   )
   ```

5. **Delete (Ein ganzes Lager löschen):**
   
   ```
   db.warehouses.deleteOne({ "warehouseID": "WH-05" })
   ```

1.

![](C:\Users\ekici\AppData\Roaming\marktext\images\2026-04-01-21-20-38-image.png)

2.

<img src="file:///C:/Users/ekici/AppData/Roaming/marktext/images/2026-04-01-21-21-25-image.png" title="" alt="" width="415">

3.

<img src="file:///C:/Users/ekici/AppData/Roaming/marktext/images/2026-04-01-21-22-04-image.png" title="" alt="" width="484">

4.

<img src="file:///C:/Users/ekici/AppData/Roaming/marktext/images/2026-04-01-21-25-52-image.png" title="" alt="" width="495">

5.

<img src="file:///C:/Users/ekici/AppData/Roaming/marktext/images/2026-04-01-21-26-19-image.png" title="" alt="" width="496">



### Teil 2: 3 Berichts-Fragestellungen (Vertiefung)

Das ist der "intelligente" Teil für die Note 1.

**Fragestellung 1: Wie ist der Lagerbestand eines Produkts über ALLE Standorte?** *Hilfreich, um zu sehen, ob man intern Ware verschieben kann.*

```
db.warehouses.aggregate([
  { $unwind: "$products" },
  { $match: { "products.productID": "P-1-10" } },
  { $group: { _id: "$products.productName", Gesamtbestand: { $sum: "$products.productQuantity" } } }
])
```

**Fragestellung 2: Welche Produkte haben einen kritischen Bestand (unter 10 Stück) über alle Lager?** *Wichtig für den Einkauf, um nachzubestellen.*

```
db.warehouses.aggregate([
  { $unwind: "$products" },
  { $match: { "products.productQuantity": { $lt: 10 } } },
  { $project: { _id: 0, Lager: "$name", Produkt: "$products.productName", Menge: "$products.productQuantity" } }
])
```

**Fragestellung 3: Wie viele Produkte jeder Kategorie haben wir insgesamt im System?** *Übersicht für das Management.*

```
db.warehouses.aggregate([
  { $unwind: "$products" },
  { $group: { _id: "$products.productCategory", Anzahl_Produkte: { $count: {} } } }
])
```



Fragestellung 1.

![](C:\Users\ekici\AppData\Roaming\marktext\images\2026-04-01-21-28-14-image.png)

Fragestellung 2.

![](C:\Users\ekici\AppData\Roaming\marktext\images\2026-04-01-21-29-09-image.png)

Fragestellung 3.

![](C:\Users\ekici\AppData\Roaming\marktext\images\2026-04-01-21-29-39-image.png)








