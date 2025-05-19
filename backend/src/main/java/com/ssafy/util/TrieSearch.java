package com.ssafy.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Trie 자료구조의 노드를 정의
class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    List<String> results = new ArrayList<>();
}

@Slf4j
public class TrieSearch {

    // Trie의 루트 노드
    static TrieNode root = new TrieNode();

    // 모든 위치에서 시작하는 부분 문자열을 Trie에 삽입하는 메서드
    public static void insert(String code, String address) {
        if (code == null || address == null || code.trim().isEmpty() || address.trim().isEmpty()) {
            return; // code 또는 address가 null이거나 빈 문자열일 경우 무시
        }

        // address의 null 값을 제거
        address = cleanNullValues(address);

        for (int i = 0; i < address.length(); i++) {
            TrieNode node = root;
            for (int j = i; j < address.length(); j++) {
                char c = address.charAt(j);
                node.children.putIfAbsent(c, new TrieNode());
                node = node.children.get(c);
                // 중복된 결과를 방지하기 위해 결과 리스트에 존재하지 않는 경우에만 추가
                if (!node.results.contains(code + ", " + address)) {
                    node.results.add(code + ", " + address);
                }
            }
        }
//        log.debug("Trie:: Inserted: " + address);
    }

    // null 값을 빈 문자열로 대체하는 헬퍼 메서드
    private static String cleanNullValues(String input) {
        return input.replaceAll("\\bnull\\b", "").replaceAll("\\s+", " ").trim();
    }

    // 검색어로 Trie에서 데이터를 검색하는 메서드
    public static List<String> search(String keyword) {
        TrieNode node = root;
        for (char c : keyword.toCharArray()) {
//            log.info("Trie:: Searching for: " + c); // 디버깅용 로그 추가
            if (!node.children.containsKey(c)) {
//                log.info("Trie:: Character not found: " + c); // 디버깅용 출력
                return new ArrayList<>();
            }
            node = node.children.get(c);
        }
        // 해당 노드에서 시작하는 모든 결과 수집 (중복 제거)
        return new ArrayList<>(node.results);
    }

    public static void main(String[] args) {
        // 데이터 삽입
        insert("1111012000", "서울특별시 종로구 신문로1가");
        insert("1111012100", "서울특별시 종로구 신문로2가");
        insert("1111012200", "서울특별시 종로구 청진동");
        insert("1111012300", "서울특별시 종로구 서린동");
        insert("1111012400", "서울특별시 종로구 수송동");

        // 검색 예시
        System.out.println("\n검색어: '신'");
        List<String> result = search("신");
        for (String s : result) {
            System.out.println(s);
        }

        System.out.println("\n검색어: '신문'");
        result = search("신문");
        for (String s : result) {
            System.out.println(s);
        }

        System.out.println("\n검색어: '신문동'");
        result = search("신문동");
        for (String s : result) {
            System.out.println(s);
        }

        System.out.println("\n검색어: '종'");
        result = search("종");
        for (String s : result) {
            System.out.println(s);
        }
    }
}