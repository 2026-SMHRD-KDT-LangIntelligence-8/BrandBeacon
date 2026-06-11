package com.example.brandbeacon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class GenerateMoodboardRequestDto {

    @JsonProperty("brand_intro")
    private String brandIntro;

    @JsonProperty("item_type")
    private String itemType;

    @JsonProperty("selected_tags")
    private List<String> selectedTags;
}