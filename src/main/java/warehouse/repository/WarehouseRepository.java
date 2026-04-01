package warehouse.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import warehouse.model.Warehouse;

public interface WarehouseRepository extends MongoRepository<Warehouse, String> {

//    public ProductData findByProductID(String productID);


    // Findet ein komplettes Lager-Dokument anhand der Geschäfts-ID (z.B. "WH-01")
    Warehouse findByWarehouseID(String warehouseID);

    // Falls du Lager nach Stadt suchen willst (optional für Berichte)
//    List<Warehouse> findByCity(String city);
}