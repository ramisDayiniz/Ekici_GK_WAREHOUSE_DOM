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

    // --- WAREHOUSE ENDPOINTS ---

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