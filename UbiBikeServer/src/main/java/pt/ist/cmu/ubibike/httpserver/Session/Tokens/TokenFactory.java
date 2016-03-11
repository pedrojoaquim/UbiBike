package pt.ist.cmu.ubibike.httpserver.Session.Tokens;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import pt.ist.cmu.ubibike.httpserver.cipher.CipherManager;
import pt.ist.cmu.ubibike.httpserver.cipher.CipherUtils;
import pt.ist.cmu.ubibike.httpserver.cipher.PrivateKeyReader;
import pt.ist.cmu.ubibike.httpserver.model.User;

import java.security.PublicKey;

/**
 * Created by ASUS on 11/03/2016.
 */
public class TokenFactory {

    public static String generatePublicKeyToken(User u){

        try {
            PublicKeyToken token = new PublicKeyToken(u);
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
            String json =  mapper.writeValueAsString(token);
            byte[] encodedToken = CipherManager.cipher(json.getBytes(), PrivateKeyReader.getKey());

            return CipherUtils.encodeToBase64String(encodedToken);

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
