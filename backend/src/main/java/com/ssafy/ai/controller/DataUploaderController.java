package com.ssafy.ai.controller;

import com.ssafy.ai.service.DongCodePineconeUploader;
import com.ssafy.ai.service.HouseInfoPineconeUploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/pinecone")
public class DataUploaderController {

    private final DongCodePineconeUploader dongCodeUploader;
    private final HouseInfoPineconeUploader houseInfoUploader;

    @Autowired
    public DataUploaderController(
            DongCodePineconeUploader dongCodeUploader,
            HouseInfoPineconeUploader houseInfoUploader) {
        this.dongCodeUploader = dongCodeUploader;
        this.houseInfoUploader = houseInfoUploader;
    }

    // 동코드 리스트를 Pinecone에 업로드
    @PostMapping("/upload/dongcode")
    public String uploadDongCodes() {
        try {
            dongCodeUploader.uploadAllDongCodesToPinecone();
            return "DongCode data successfully uploaded to Pinecone.";
        } catch (Exception e) {
            log.error("Failed to upload DongCode data: {}", e.getMessage());
            return "Failed to upload DongCode data to Pinecone.";
        }
    }

    // HouseInfos 리스트를 Pinecone에 업로드
    @PostMapping("/upload/houseinfo")
    public String uploadHouseInfos() {
        try {
            houseInfoUploader.uploadAllHouseInfosToPinecone();
            return "HouseInfo data successfully uploaded to Pinecone under namespace 'houseinfo'.";
        } catch (Exception e) {
            log.error("Error uploading HouseInfo data: {}", e.getMessage());
            return "Failed to upload HouseInfo data to Pinecone.";
        }
    }
}

