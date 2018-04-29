package origo.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLDocument;
import de.l3s.boilerpipe.sax.HTMLFetcher;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static origo.parser.Main.links;

public class ElasticSetup {
    private static final String PATH = "./src/main/resources/";

    public static void main(String[] args) throws IOException {
        indexMapper("korrupcio");
        elasticConnector(HTMLparser.getURLs(links), "korrupció");
    }

    public static void indexMapper(String tag) {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")
                ));
        ObjectMapper mapper = new ObjectMapper();
        File from = new File(PATH + "template.json");
        TypeReference<HashMap<String,Object>> typeRef
                = new TypeReference<HashMap<String,Object>>() {};

        HashMap<String,Object> map = null;
        try {
            map = mapper.readValue(from, typeRef);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(tag);
        try {
            client.indices().create(createIndexRequest.mapping("doc", map));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void elasticConnector(List relevantUrls, String tag) throws IOException {
        SimpleDateFormat parser=new SimpleDateFormat("yyyy.MM.dd");
        Date date = null;
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")
                ));
        IndexRequest indexRequest = new IndexRequest(tag, "doc");
        for (int i = 0; i < relevantUrls.size(); i++) {
            final String regex = "\\d{4}\\.(0?[1-9]|1[012])\\.(..)";
            final HTMLDocument htmlDoc = HTMLFetcher.fetch(new URL((String) relevantUrls.get(i)));
            TextDocument doc = null;
            try {
                doc = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
            } catch (BoilerpipeProcessingException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            URL url = new URL((String) relevantUrls.get(i));
            String text = null;
            try {
                text = ArticleExtractor.INSTANCE.getText(url);
            } catch (BoilerpipeProcessingException e) {
                e.printStackTrace();
            }
            Map <String, Object> jsonMap = new HashMap <>();
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                jsonMap.put("date", matcher.group(0));
                System.out.println(matcher.group(0));
            }
            jsonMap.put("title", doc.getTitle());
            jsonMap.put("content", text);
            try {
                client.index(indexRequest.source(jsonMap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
