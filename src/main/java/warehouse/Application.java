package warehouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import warehouse.model.ProductData;
import warehouse.model.Warehouse;
import warehouse.repository.WarehouseRepository;

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


//	@Override
//	public void run(String... args) throws Exception {
//
//		// Initialize product data repository
//		repository.deleteAll();
//
//		// save a couple of product data
//		repository.save(new ProductData("1","00-443175","Bio Orangensaft Sonne","Getraenk", 2500));
//		repository.save(new ProductData("1","00-871895","Bio Apfelsaft Gold","Getraenk", 3420));
//		repository.save(new ProductData("1","01-926885","Ariel Waschmittel Color","Waschmittel", 478));
//		repository.save(new ProductData("1","02-234811","Mampfi Katzenfutter Rind","Tierfutter", 1324));
//		repository.save(new ProductData("2","03-893173","Saugstauberbeutel Ingres","Reinigung", 7390));
//		System.out.println();
//
//		// fetch all products
//		System.out.println("ProductData found with findAll():");
//		System.out.println("-------------------------------");
//		for (ProductData productdata : repository.findAll()) {
//			System.out.println(productdata);
//		}
//		System.out.println();
//
//		// Fetch single product
//		System.out.println("Record(s) found with ProductID(\"00-871895\"):");
//		System.out.println("--------------------------------");
//		System.out.println(repository.findByProductID("00-871895"));
//		System.out.println();
//
//		// Fetch all products of Warehouse 1
//		System.out.println("Record(s) found with findByWarehouseID(\"1\"):");
//		System.out.println("--------------------------------");
//		for (ProductData productdata : repository.findByWarehouseID("1")) {
//			System.out.println(productdata);
//		}
//		System.out.println();
//
//		// Fetch all products of Warehouse 2
//		System.out.println("Record(s) found with findByWarehouseID(\"2\"):");
//		System.out.println("--------------------------------");
//		for (ProductData productdata : repository.findByWarehouseID("2")) {
//			System.out.println(productdata);
//		}
//
//	}



}
