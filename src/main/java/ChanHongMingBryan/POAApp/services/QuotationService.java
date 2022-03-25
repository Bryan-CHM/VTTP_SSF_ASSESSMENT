package ChanHongMingBryan.POAApp.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.util.JSONPObject;

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

        
        String url = UriComponentsBuilder				// To configure url and parameters
        .fromUriString(QUOTATIONS_URL)
        .toUriString();
        
        JsonArrayBuilder reqJsonBuilder = Json.createArrayBuilder();
        for(String s : items){
            
            reqJsonBuilder.add(s.replace("\"", ""));
        }   
        JsonArray reqJson = reqJsonBuilder.build();
        
        RequestEntity<String> req = RequestEntity	// To configure HTTP request
        .post(url)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(reqJson.toString(),String.class);
    
        RestTemplate template = new RestTemplate(); 
        ResponseEntity <String> resp = template.exchange(req, String.class);

        JsonObject respJson;
        Quotation quotation = new Quotation();
        try (InputStream is = new ByteArrayInputStream (resp.getBody().getBytes())){
            JsonReader reader = Json.createReader(is);
            respJson = reader.readObject();
            quotation.setQuoteId(respJson.getString("quoteId"));
            JsonArray quotations = respJson.getJsonArray("quotations");
            Map<String, Float> quotationsAsMap = new HashMap<>();
            if (quotations != null) { 
                for (int i=0;i<quotations.size();i++){ 
                    quotationsAsMap
                    .put(quotations.getJsonObject(i).getString("item"), 
                    Float.parseFloat(quotations.getJsonObject(i).get("unitPrice").toString()));
                }
            }
            quotation.setQuotations(quotationsAsMap);
            }   catch (Exception e) {
                    //TODO: handle exception
            e.printStackTrace();
        }
        return Optional.of(quotation);
        
    }
    public Float calculateTotal(Map<String, Integer> quantity, Map<String, Float> unitPrice){
        Float total = 0F;
        if(!quantity.isEmpty()&&!unitPrice.isEmpty() && quantity.size()==unitPrice.size()){
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
