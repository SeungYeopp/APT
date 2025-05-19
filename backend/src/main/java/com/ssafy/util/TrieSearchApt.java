package com.ssafy.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Trie 자료구조의 노드를 정의
class TrieNodeApt {
    Map<Character, TrieNodeApt> children = new HashMap<>();
    List<String> results = new ArrayList<>();
}

@Slf4j
public class TrieSearchApt {

    // Trie의 루트 노드
    static TrieNodeApt root = new TrieNodeApt();

    // 아파트 이름을 Trie에 삽입하는 메서드
    public static void insert(String aptName) {
        if (aptName == null || aptName.trim().isEmpty()) {
            return; // aptName이 null이거나 빈 문자열일 경우 무시
        }

        aptName = aptName.trim();

        for (int i = 0; i < aptName.length(); i++) {
            TrieNodeApt node = root;
            for (int j = i; j < aptName.length(); j++) {
                char c = aptName.charAt(j);
                node.children.putIfAbsent(c, new TrieNodeApt());
                node = node.children.get(c);
                // 중복된 결과를 방지하기 위해 결과 리스트에 존재하지 않는 경우에만 추가
                if (!node.results.contains(aptName)) {
                    node.results.add(aptName);
                }
            }
        }
    }

    // 검색어로 Trie에서 데이터를 검색하는 메서드
    public static List<String> search(String keyword) {
        TrieNodeApt node = root;
        for (char c : keyword.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return new ArrayList<>();
            }
            node = node.children.get(c);
        }
        // 해당 노드에서 시작하는 모든 결과 수집 (중복 제거)
        return new ArrayList<>(node.results);
    }
}
