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





# Praxis

### Model

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
package warehouse.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

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



### Controller

Nach den Model Klassen brauchen wir natürlich für Rest Schnittstellen noch eine Controller Klasse, wo wir alle REST-Funktionen implementieren.

```java
package warehouse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import warehouse.model.ProductData;
import warehouse.model.Warehouse;
import warehouse.repository.WarehouseRepository;
import java.util.List;
import java.util.stream.Collectors;

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


