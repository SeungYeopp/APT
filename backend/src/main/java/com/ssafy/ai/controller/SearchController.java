package com.ssafy.ai.controller;

import com.ssafy.ai.service.PineconeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final PineconeService searchService;

    @Autowired
    public SearchController(PineconeService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/dong")
    public List<PineconeService.SearchResult> searchDong(@RequestParam String dongName) {
        return searchService.searchByDongName(dongName);
    }

    @GetMapping("/houseinfo")
    public List<PineconeService.SearchResult> searchHouseInfo(@RequestParam String query) {
        return searchService.searchHouseInfo(query);
    }
}