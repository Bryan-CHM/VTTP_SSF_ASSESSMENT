package ChanHongMingBryan.POAApp.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ChanHongMingBryan.POAApp.models.Quotation;
import ChanHongMingBryan.POAApp.services.QuotationService;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@RestController
@RequestMapping(path="/api")
public class PurchaseOrderRestController {

    @Autowired
    private QuotationService QuotationSvc;

    @PostMapping(path="/po",consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postPurchaseOrder(@RequestBody String payload){

        JsonObject poJson;
        try {
            // Read in input as Json 
            InputStream is = new ByteArrayInputStream (payload.getBytes());
            JsonReader reader = Json.createReader(is);
            poJson = reader.readObject();

            // Variables to store information from JsonObject
            JsonArray lineItems = poJson.getJsonArray("lineItems");
            List<String> fruits = new LinkedList<>();
            Map<String, Integer> quantity = new HashMap<>();


            // Storing information from JsonObject
            if (lineItems != null) { 
                for (int i=0;i<lineItems.size();i++){ 
                fruits.add(lineItems.getJsonObject(i).get("item").toString().replace("\"",""));
                quantity.put(lineItems.getJsonObject(i).get("item").toString().replace("\"",""),lineItems.getJsonObject(i).getInt("quantity"));
                }
            }
            
            // Calling QuotationService to get quotations and quoteId
            Optional<Quotation> quotation = QuotationSvc.getQuotations(fruits);

            // Calling QuotationService to calculate total
            Float total = QuotationSvc.calculateTotal(quantity,quotation.get().getQuotations());

            // Building the JsonObject to send back to client
            JsonObject clientJson= Json.createObjectBuilder()
            .add("invoiceId",quotation.get().getQuoteId())
            .add("name", poJson.getString("name"))
            .add("total",total)
            .build();
            return ResponseEntity.ok(clientJson.toString());
        }
        catch(Exception ex) {
            poJson = Json.createObjectBuilder().add("Error", ex.getMessage()).build();
            return (ResponseEntity.internalServerError().body(poJson.toString()));
        }
    }
}
