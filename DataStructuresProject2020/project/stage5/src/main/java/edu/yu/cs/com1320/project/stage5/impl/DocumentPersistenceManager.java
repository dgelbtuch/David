package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File basDir;
    private JsonSerializer<Document> serializer = new JsonSerializer<Document>() {
        @Override
        public JsonElement serialize(Document document, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonDoc = new JsonObject();
            Gson gson = new Gson();
            jsonDoc.addProperty("text", document.getDocumentAsTxt());
            jsonDoc.addProperty("URI", document.getKey().toString());
            jsonDoc.addProperty("hashcode", document.getDocumentTextHashCode());
            jsonDoc.addProperty("wordMap", gson.toJson(document.getWordMap()));
            return jsonDoc;
        }
    };
    private JsonDeserializer<Document> deserializer = new JsonDeserializer<Document>() {
        @Override
        public Document deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            String text = jsonElement.getAsJsonObject().get("text").getAsString();
            URI uri = null;
            try {
                uri = new URI(jsonElement.getAsJsonObject().get("URI").getAsString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            int hashcode = jsonElement.getAsJsonObject().get("hashcode").getAsInt();
            Gson gson = new Gson();
            String mapString = jsonElement.getAsJsonObject().get("wordMap").getAsString();
            Type mapType = new TypeToken<HashMap<String,Integer>>(){}.getType();
            HashMap<String, Integer> map = gson.fromJson(mapString, mapType);
            DocumentImpl document = new DocumentImpl(uri, text, hashcode, map);
            return document;
        }
    };

    public DocumentPersistenceManager(File baseDir){
        this.basDir = baseDir;
        if(baseDir == null){
            this.basDir = new File (System.getProperty("user.dir"));
        }
        if(!this.basDir.exists()){
            this.basDir.mkdirs();
        }
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        String path = "";
        if(uri.getRawAuthority() != null){
            path = File.separator + uri.getRawAuthority();
        }
        path += uri.getRawPath();
        if(!path.startsWith(File.separator)){
            path = File.separator + path;
        }
        File directory = new File(this.basDir + path + ".json");
        directory.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(this.basDir + path + ".json");
        Type documentType = new TypeToken<Document>(){}.getType();
        Gson gson = new GsonBuilder().registerTypeAdapter(Document.class, serializer).setPrettyPrinting().create();
        gson.toJson(val, documentType, writer);
        writer.close();
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        String path = "";
        if(uri.getRawAuthority() != null){
            path = File.separator + uri.getRawAuthority();
        }
        path += uri.getRawPath();
        if(!path.startsWith(File.separator)){
            path = File.separator + path;
        }
        File exist = new File (basDir + path + ".json");
        if(!exist.exists()){
            return null;
        }

        FileReader reader = new FileReader(basDir + path + ".json");
        Gson gson = new GsonBuilder().registerTypeAdapter(Document.class, deserializer).create();
        Type documentType = new TypeToken<Document>(){}.getType();
        DocumentImpl document = gson.fromJson(reader, documentType);
        reader.close();
        File deleteFile = new File(basDir + path + ".json");
        deleteDir(deleteFile);
        return document;
    }
    private void deleteDir(File file){
        String dirs = file.getParent();
        file.delete();
        if(dirs == null){
            return;
        }
        File parent = new File(dirs);
        if(parent.list().length == 0){
            deleteDir(parent);
        }
    }
}
