package com.knut4.backend.domain.recommendation;

import com.knut4.backend.domain.llm.LlmClient;
import com.knut4.backend.domain.llm.LlmMenuSuggestion;
import com.knut4.backend.domain.llm.StructuredMenuPlace;
import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Verifies that structured place ordering prioritizes LLM selected places. */
public class PlacePriorityOrderingTest {
    @Test
    void prioritizesStructuredPlaces() {
        MapProvider mapProvider = (k,lat,lon,r) -> List.of(
                new PlaceResult("PlaceA",0,0,"",100),
                new PlaceResult("PlaceB",0,0,"",200),
                new PlaceResult("PlaceC",0,0,"",300)
        );
        LlmClient llm = new LlmClient() {
            public List<LlmMenuSuggestion> suggestMenus(List<String>a,String b,Integer c,Double d,Double e,List<String> f,int g){
                return List.of(new LlmMenuSuggestion("TestMenu","이유"));
            }
            public List<StructuredMenuPlace> suggestMenusWithPlaces(List<String>a,String b,Integer c,Double d,Double e,String j,int g){
                return List.of(new StructuredMenuPlace("TestMenu", List.of("PlaceC","PlaceA"), "이유"));
            }
        };
        // Create minimal RecommendationService with null repositories (only methods used in test avoid NPEs)
    RecommendationService svc = new RecommendationService(mapProvider, null, llm, null, null, null, null, false);
        RecommendationRequest req = new RecommendationRequest("맑음", List.of(), 10000, 0.0,0.0);
        RecommendationResponse resp = svc.recommend(req);
        var places = resp.menuRecommendations().get(0).places();
        assertEquals("PlaceC", places.get(0).name());
        assertEquals("PlaceA", places.get(1).name());
    }
}
