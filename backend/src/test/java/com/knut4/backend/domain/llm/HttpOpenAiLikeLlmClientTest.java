package com.knut4.backend.domain.llm;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Tests focused on parsing robustness of HttpOpenAiLikeLlmClient (JSON vs fallback line). */
public class HttpOpenAiLikeLlmClientTest {

    @Test
    void parseStructuredJson_valid() throws Exception {
        String json = "[ {\"menu\":\"비빔밥\", \"reason\":\"가볍고 다양한 재료\", \"places\":[{\"name\":\"한식당A\"},{\"name\":\"한식당B\"}]}, {\"menu\":\"라멘\", \"reason\":\"따뜻하고 든든\", \"places\":[{\"name\":\"라멘집\"}]} ]";
        // Use reflection to access parseStructuredJson (package-private in same package if left default). Here we mimic by creating subclass exposing method.
        class Tester extends HttpOpenAiLikeLlmClient {
            Tester(){ super("http://localhost", "test"); }
            List<StructuredMenuPlace> call(String c){ return super.parseStructuredJson(c, 5); }
        }
        Tester t = new Tester();
        List<StructuredMenuPlace> out = t.call(json);
        assertEquals(2, out.size());
        assertEquals("비빔밥", out.get(0).menu());
        assertEquals(List.of("한식당A","한식당B"), out.get(0).places());
    }

    @Test
    void parseStructuredJson_malformedFallsBack() {
        String malformed = "Not JSON but maybe text line with menu | place1,place2 | 이유";
        class Tester extends HttpOpenAiLikeLlmClient {
            Tester(){ super("http://localhost", "test"); }
            List<StructuredMenuPlace> call(String c){ return super.parseStructuredJson(c, 3); }
        }
        Tester t = new Tester();
        List<StructuredMenuPlace> out = t.call(malformed);
        assertEquals(1, out.size());
        assertEquals("Not JSON but maybe text line with menu", out.get(0).menu());
        assertEquals(List.of("place1","place2"), out.get(0).places());
    }
}
