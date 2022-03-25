package ChanHongMingBryan.POAApp.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ChanHongMingBryan.POAApp.models.Quotation;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class QuotationService {
    private final String QUOTATIONS_URL = "https://quotation.chuklee.com/quotation";

    public Optional<Quotation> getQuotations(List<String> items){

        // To configure url and parameters if any
        String url = UriComponentsBuilder				
        .fromUriString(QUOTATIONS_URL)
        .toUriString();
        
        // To build JsonObject to be sent
        JsonArrayBuilder reqJsonBuilder = Json.createArrayBuilder();
        for(String s : items){  
            reqJsonBuilder.add(s);
        }   
        JsonArray reqJson = reqJsonBuilder.build();
        
        // To configure HTTP request with required parameters and JsonObject as payload
        RequestEntity<String> req = RequestEntity	
        .post(url)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(reqJson.toString(),String.class);
    
        // To make HTTP request and store HTTP response 
        RestTemplate template = new RestTemplate(); 
        ResponseEntity <String> resp = template.exchange(req, String.class);

        // Variables to store information from JsonObject
        JsonObject respJson;
        Quotation quotation = new Quotation();
        Map<String, Float> quotationsAsMap = new HashMap<>();
        
        // To read response as JsonObject 
        InputStream is = new ByteArrayInputStream (resp.getBody().getBytes());
        JsonReader reader = Json.createReader(is);
        respJson = reader.readObject();

        // Storing information from JsonObject
        quotation.setQuoteId(respJson.getString("quoteId"));
        JsonArray quotations = respJson.getJsonArray("quotations");
        if (quotations != null) { 
            for (int i=0;i<quotations.size();i++){ 
                quotationsAsMap
                .put(quotations.getJsonObject(i).getString("item"), 
                Float.parseFloat(quotations.getJsonObject(i).get("unitPrice").toString()));
            }
        }
        quotation.setQuotations(quotationsAsMap);
            
        return Optional.of(quotation);
        
    }
    public Float calculateTotal(Map<String, Integer> quantity, Map<String, Float> unitPrice){
        
        Float total = 0F;
        // Checking if both maps are NOT empty and that their size matches each other
        if(!quantity.isEmpty()&&!unitPrice.isEmpty() && quantity.size()==unitPrice.size()){
            /*
             * Iterate through both maps and find the common keys(fruits)
             * If the keys match, take the quantity of the first map, multiply by unitPrice of second map
             * Add to total after each iteration
             */
            for(var quantityIterator : quantity.entrySet()){
                for(var unitPriceIterator : unitPrice.entrySet()){
                    if(quantityIterator.getKey().equals(unitPriceIterator.getKey())){
                        total+=quantityIterator.getValue()*unitPriceIterator.getValue();
                    }
                }
            }     
        }
        return total;
    }
}
